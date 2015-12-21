package org.percepta.mgrankvi;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import org.apache.commons.codec.binary.Base64;
import org.percepta.mgrankvi.client.geometry.Line;
import org.percepta.mgrankvi.client.geometry.Point;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Utility to get the lines in a image file to generate lines for the VisibilityMap.
 *
 * @author Mikael Grankvist - Vaadin }>
 */
public class ImageToLines {

    /**
     * Get lines for given image.
     *
     * @param imageFile File containing "map" to generate lines from
     * @return List of lines found in image.
     */
    public List<Line> getLines(String imageFile) {
        BufferedImage image = getImage(imageFile);

        // If no image found cancel and return empty list
        if (image == null)
            return Lists.newLinkedList();

        // Get corner points
        List<Point> points = getCornerPoints(image);

        return getLinesForPoints(image, points);
    }

    /**
     * Load the image from classpath or file system.
     *
     * @param filename File to get image for.
     * @return File or null if not found.
     */
    private BufferedImage getImage(String filename) {
        // load the file using Java's imageIO library
        BufferedImage image = null;
        try {
            URL resource = getClass().getResource(filename);
            if (resource != null) {
                image = javax.imageio.ImageIO.read(resource);
            } else {
                image = javax.imageio.ImageIO.read(new File(filename));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Get corner points for image.
     * Uses HarrisFast corner detection.
     *
     * @param image Image to search for points
     * @return
     */
    private static List<Point> getCornerPoints(BufferedImage image) {
        List<HarrisFast.Corner> corners = getCorners(image);

        List<Point> points = Lists.newLinkedList();
        for (HarrisFast.Corner corner : corners) {
            points.add(new Point(corner.x, corner.y));
        }
        return points;
    }

    /**
     * Generate lines from found points by referring to the image to check if a line should be drawn.
     * @param image
     * @param points
     * @return
     */
    private List<Line> getLinesForPoints(BufferedImage image, List<Point> points) {
        List<Line> lines = Lists.newLinkedList();
        List<Point> pointsLeft = Lists.newArrayList(points);

        //Luminance threshold
        double luminance = 25.0;
        // Iterate from point to point and reference to image to see if we have a line between the points.
        for (Point p : points) {
            pointsLeft.remove(p);
            for (Point p2 : pointsLeft) {
                double min;
                double max;
                boolean foundLine = true;

                if (p.getX() == p2.getX()) {
                    min = Math.min(p.getY(), p2.getY());
                    max = Math.max(p.getY(), p2.getY());
                    for (double y = min; y < max; y++) {
                        if (getLuminance(image.getRGB((int) p.getX(), (int) y)) > luminance) {
                            foundLine = false;
                            break;
                        }
                    }
                } else if (p.getY() == p2.getY()) {
                    min = Math.min(p.getX(), p2.getX());
                    max = Math.max(p.getX(), p2.getX());
                    for (double x = min; x < max; x++) {
                        if (getLuminance(image.getRGB((int) x, (int) p.getY())) > luminance) {
                            foundLine = false;
                            break;
                        }
                    }
                } else {
                    Double m = getSlope(p, p2);
                    Double b = intercept(p, m);

                    min = Math.min(p.getX(), p2.getX());
                    max = Math.max(p.getX(), p2.getX());
                    for (double x = min; x <= max; x += 0.1) {
                        double y = m * x + b;
                        // Not on a line if even one non black pixel is found on line
                        if (getLuminance(image.getRGB((int) x, (int) y)) > luminance) {
                            foundLine = false;
                            break;
                        }
                    }
                }

                if (foundLine) {
                    Line line = new Line(p, p2);
                    if (!lines.contains(line)) {
                        lines.add(line);
                    }
                }
            }
        }
        return lines;
    }

    /**
     * Calculate the slope for the line between two points
     *
     * @param a Point A
     * @param b Point B
     * @return Slope of line
     */
    private static Double getSlope(Point a, Point b) {
        if (a.getX() == b.getX()) {
            return 0.0;
        }
        return (a.getY() - b.getY()) / (a.getX() - b.getX());
    }

    /**
     * Get the y-intercept value for point and slope
     * @return y-intercept point
     */
    private static Double intercept(Point p, Double slope) {
        if (slope == null) {
            return p.getX();
        }
        return p.getY() - slope * p.getX();
    }

    /**
     * Scan for corners in image using the Harris Fast Scan algorithm.
     * @param image Image to search for corners
     * @return corners found
     */
    private static List<HarrisFast.Corner> getCorners(BufferedImage image) {
        if (image == null) return Lists.newArrayList();
        int width = image.getWidth();    // largeur de l'image
        int height = image.getHeight();    // hauteur de l'image
        int[][] input = new int[width][height];        // tableau 2D [x][y] contenant l'image en niveau de gris (0-255)

        for (int i = 0; i < width - 1; i++) {
            for (int j = 0; j < height - 1; j++) {
                input[i][j] = (int) getLuminance(image.getRGB(i, j));
            }
        }

        //////////////////////

        double sigma = 1.2;  // parametre du filtre gaussien
        double k = 0.06; // parametre de la formule de la mesure
        int spacing = 8;    // distance minimum entre 2 coins

        // Init
        HarrisFast hf = new HarrisFast(input, width, height);
        // run filter to get corners.
        hf.filter(sigma, k, spacing);

        List<HarrisFast.Corner> corners = hf.corners;
        // Remove the last corner as it is always the lower right corner of image enen though there was no point there.
        corners.remove(corners.size() - 1);

        // Uncomment if a base64 image is needed for debugging purposes!!!
//        BufferedImage bufferedImage = duplicateImage(image);
//        Graphics2D g2d = bufferedImage.createGraphics();
//        g2d.setColor(Color.RED);
//        int nr = 1;
//
//        for (HarrisFast.Corner corner : corners) {
//            g2d.fill(new Rectangle2D.Float(corner.x - 5, corner.y - 5, 10, 10));
//            g2d.drawString("" + nr++, corner.x - 10, corner.y - 10);
//        }
//        g2d.dispose();
//        System.out.println(encodeImageToBase64(bufferedImage));

        return corners;
    }

    /**
     * Clones the given BufferedImage
     *
     * @param sourceImage The image to copy
     * @return A copy of sourceImage
     */
    public static BufferedImage duplicateImage(BufferedImage sourceImage) {
        // This method could likely be optimized but the gain is probably small
        int w = sourceImage.getWidth();
        int h = sourceImage.getHeight();

        BufferedImage newImage = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = (Graphics2D) newImage.getGraphics();
        g.drawImage(sourceImage, 0, 0, w, h, null);
        g.dispose();

        return newImage;
    }

    /**
     * Get the luminance value for given rgb value
     * @param rgb RGB value to get luminance for
     * @return Luminance
     */
    public static double getLuminance(int rgb) {
        int r = ((rgb >> 16) & 0xFF);
        int g = ((rgb >> 8) & 0xFF);
        int b = (rgb & 0xFF);

        return getLuminance(r, g, b);
    }

    /**
     * Get the luminance value for given rgb value
     * @param r Red value
     * @param g Green value
     * @param b Blue value
     * @return Luminance
     */
    private static double getLuminance(int r, int g, int b) {
        return .299 * r + .587 * g + .114 * b;
    }

    /**
     * Encode image to a Base64 string.
     * @param image Image to encode
     * @return encoded image
     */
    public static String encodeImageToBase64(BufferedImage image) {
        String encodedImage = "";
        Base64 encoder = new Base64();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            baos.flush();
            byte[] encodedBytes = encoder.encode(baos.toByteArray());
            encodedImage = new String(encodedBytes);
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encodedImage;
    }
}
