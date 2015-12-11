package org.percepta.mgrankvi.client.geometry;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Calculations {


    public static Intersect getIntersection(Line ray, Line segment) {
        Parametric r = new Parametric(ray);
        Parametric s = new Parametric(segment);

        if (areParallel(r, s)) return null;

        double t2 = (r.directionX * (s.pointY - r.pointY) + r.directionY * (r.pointX - s.pointX)) / (s.directionX * r.directionY - s.directionY * r.directionX);
        double t1 = (s.pointX + s.directionX * t2 - r.pointX) / r.directionX;

        if (t1 < 0) return null;
        if (t2 < 0 || t2 > 1) return null;

        return new Intersect(new Point(r.pointX+r.directionX*t1, r.pointY+r.directionY*t1), t1);
    }

    private static boolean areParallel(Parametric line1, Parametric line2) {
        double r_mag = Math.sqrt(line1.directionX * line1.directionX + line1.directionY * line1.directionY);
        double s_mag = Math.sqrt(line2.directionX * line2.directionX + line2.directionY * line2.directionY);
        if (line1.directionX / r_mag == line2.directionX / s_mag && line1.directionY / r_mag == line2.directionY / s_mag) {
            return true;
        }
        return false;
    }

    private static class Parametric {
        public double pointX;
        public double pointY;
        public double directionX;
        public double directionY;

        public Parametric(Line line) {
            pointX = line.start.getX();
            pointY = line.start.getY();
            directionX = line.end.getX() - line.start.getX();
            directionY = line.end.getY() - line.start.getY();
        }
    }
}
