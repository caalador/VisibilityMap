package org.percepta.mgrankvi.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import org.percepta.mgrankvi.client.geometry.Calculations;
import org.percepta.mgrankvi.client.geometry.Intersect;
import org.percepta.mgrankvi.client.geometry.Line;
import org.percepta.mgrankvi.client.geometry.Point;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Extend any GWT Widget
public class MyComponentWidget extends Composite implements MouseMoveHandler {

    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;

    Canvas map;

    List<Line> lines = new LinkedList<Line>() {{
        // Border
        add(new Line(new Point(0, 0), new Point(WIDTH, 0)));
        add(new Line(new Point(WIDTH, 0), new Point(WIDTH, HEIGHT)));
        add(new Line(new Point(WIDTH, HEIGHT), new Point(0, HEIGHT)));
        add(new Line(new Point(0, HEIGHT), new Point(0, 0)));

        // polygon 1
        add(new Line(new Point(100, 150), new Point(120, 50)));
        add(new Line(new Point(120, 50), new Point(200, 80)));
        add(new Line(new Point(200, 80), new Point(140, 210)));
        add(new Line(new Point(140, 210), new Point(100, 150)));

        // polygon 2
        add(new Line(new Point(100, 200), new Point(120, 250)));
        add(new Line(new Point(120, 250), new Point(60, 300)));
        add(new Line(new Point(60, 300), new Point(100, 200)));

        // Polygon #3
        add(new Line(new Point(200, 260), new Point(220, 150)));
        add(new Line(new Point(220, 150), new Point(300, 200)));
        add(new Line(new Point(300, 200), new Point(350, 320)));
        add(new Line(new Point(350, 320), new Point(200, 260)));

        // Polygon #4
        add(new Line(new Point(340, 60), new Point(360, 40)));
        add(new Line(new Point(360, 40), new Point(370, 70)));
        add(new Line(new Point(370, 70), new Point(340, 60)));

        // Polygon #5
        add(new Line(new Point(450, 190), new Point(560, 170)));
        add(new Line(new Point(560, 170), new Point(540, 270)));
        add(new Line(new Point(540, 270), new Point(430, 290)));
        add(new Line(new Point(430, 290), new Point(450, 190)));

        // Polygon #6
        add(new Line(new Point(400, 95), new Point(580, 50)));
        add(new Line(new Point(580, 50), new Point(480, 150)));
        add(new Line(new Point(480, 150), new Point(400, 95)));
    }};

    int x = WIDTH / 2;
    int y = HEIGHT / 2;

    public MyComponentWidget() {


        SimplePanel baseContent = new SimplePanel();
        AbsolutePanel absolute = new AbsolutePanel();
        baseContent.add(absolute);

        baseContent.getElement().getStyle().setBackgroundColor("green");
        absolute.setWidth(WIDTH + "px");
        absolute.setHeight(HEIGHT + "px");

        map = Canvas.createIfSupported();
        if (map != null) {
            Canvas bg = Canvas.createIfSupported();
            bg.setWidth(WIDTH + "px");
            bg.setHeight(HEIGHT + "px");
            bg.setCoordinateSpaceWidth(WIDTH);
            bg.setCoordinateSpaceHeight(HEIGHT);
            DrawUtil.drawGrid(bg.getContext2d(), WIDTH, HEIGHT);


            map.setWidth(WIDTH + "px");
            map.setHeight(HEIGHT + "px");

            map.addDomHandler(this, MouseMoveEvent.getType());
            paint();

            absolute.add(bg, 0, 0);
            absolute.add(map, 0, 0);
        }
        initWidget(baseContent);
    }

    private void paint() {
        clearCanvas();
        Context2d context = map.getContext2d();

        List<List<Intersect>> intersects = new LinkedList<List<Intersect>>();

        int fuzzyRadius = 10;
        for (double angle = 0; angle < Math.PI * 2; angle += (Math.PI * 2) / 10) {
            double dx = Math.cos(angle) * fuzzyRadius;
            double dy = Math.sin(angle) * fuzzyRadius;
            intersects.add(getSightPolygons(x + dx, y + dy));
        }
        drawPolygon(context, getSightPolygons(x, y), "rgba(255,255,255,0.2)");
        drawPolygons(context, intersects);

        // Segment lines
        context.setStrokeStyle("orange");
        Map<String,List<Point>>lines = new HashMap<String, List<Point>>();
        for(List<Intersect> is: intersects){
        for(Intersect i : is) {
            if(lines.containsKey(i.line.toString())){
                lines.get(i.line.toString()).add(i.getIntersectionPoint());
            } else {
                List<Point> points = new LinkedList<Point>();
                points.add(i.getIntersectionPoint());
                lines.put(i.line.toString(), points);
            }
        }}
        for(Map.Entry<String, List<Point>> entry: lines.entrySet()) {
            context.save();
            context.beginPath();
            context.setLineWidth(2.0);
            context.moveTo(entry.getValue().get(0).getX(), entry.getValue().get(0).getY());
            for(Point p : entry.getValue()) {
                context.lineTo(p.getX(),p.getY());
            }
            context.closePath();
            context.stroke();
            context.restore();
        }

        // Test having hidden points in map that only shown when "visible"
        context.setGlobalCompositeOperation("source-atop");

        context.setFillStyle("#000");
        context.beginPath();
        context.arc(50, 50, 2, 0, 2 * Math.PI, false);
        context.closePath();
        context.fill();

        context.beginPath();
        context.arc(450, 50, 2, 0, 2 * Math.PI, false);
        context.closePath();
        context.fill();
        context.setGlobalCompositeOperation("source-over");

//        // draw line
//        context.setStrokeStyle("#f55");
//        context.setFillStyle("#dd3838");
//        for (Intersect intersect : intersectList) {
//            Point intersection = intersect.getIntersectionPoint();
//            context.beginPath();
//            context.moveTo(x, y);
//            context.lineTo(intersection.getX(), intersection.getY());
//            context.closePath();
//            context.stroke();
//
//            // mouse
//            context.beginPath();
//            context.arc(intersection.getX(), intersection.getY(), 4, 0, 2 * Math.PI, false);
//            context.closePath();
//            context.fill();
//        }
        // Draw red dots for lines of sight.
        context.setFillStyle("#dd3838");
        context.beginPath();
        context.arc(x, y, 2, 0, 2 * Math.PI, false);
        context.fill();
        for (double angle = 0; angle < Math.PI * 2; angle += (Math.PI * 2) / 10) {
            double dx = Math.cos(angle) * fuzzyRadius;
            double dy = Math.sin(angle) * fuzzyRadius;
            context.beginPath();
            context.arc(x + dx, y + dy, 2, 0, 2 * Math.PI, false);
            context.closePath();
            context.fill();
        }
    }

    private void drawPolygons(Context2d context, List<List<Intersect>> intersects) {
        for (List<Intersect> polygon : intersects) {
            drawPolygon(context, polygon, "rgba(255,255,255,0.2)");
        }
    }

    private void drawPolygon(Context2d context, List<Intersect> intersectList, String fillStyle) {
        // Area Polygon

        context.setFillStyle(fillStyle);
        context.beginPath();
        Iterator<Intersect> intersects = intersectList.iterator();
        Point intersectionPoint = intersects.next().getIntersectionPoint();
        context.moveTo(intersectionPoint.getX(), intersectionPoint.getY());
        while (intersects.hasNext()) {
            intersectionPoint = intersects.next().getIntersectionPoint();
            context.lineTo(intersectionPoint.getX(), intersectionPoint.getY());
        }
        context.closePath();
        context.fill();
    }

    private LinkedList<Intersect> getSightPolygons(double x, double y) {
        List<Double> angles = getUniqueAngles(x, y);

        LinkedList<Intersect> intersectList = new LinkedList<Intersect>();
        for (Double angle : angles) {
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);
            Line ray = new Line(new Point(x, y), new Point(x + dx, y + dy));

            Intersect closestIntersectForRay = getClosestIntersectForRay(ray);
            if (closestIntersectForRay == null)
                continue;
            closestIntersectForRay.setAngle(angle);
            intersectList.add(closestIntersectForRay);
        }

        Collections.sort(intersectList, new Comparator<Intersect>() {
            @Override
            public int compare(Intersect o1, Intersect o2) {
                return Double.compare(o1.getAngle(), o2.getAngle());
            }
        });
        return intersectList;
    }

    /**
     * Find the closest intersecting segment for given ray.
     *
     * @param ray
     * @return
     */
    private Intersect getClosestIntersectForRay(Line ray) {
        Intersect closest = null;
        // Closest intersection
        for (Line l : lines) {
            Intersect i = Calculations.getIntersection(ray, l);
            if (i == null) continue;
            if (closest == null || i.getT1() < closest.getT1()) {
                closest = i;
                closest.line = l;
            }
        }
        return closest;
    }

    protected void clearCanvas() {
        map.setCoordinateSpaceWidth(WIDTH);
        map.setCoordinateSpaceHeight(HEIGHT);
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        x = event.getRelativeX(map.getElement());
        y = event.getRelativeY(map.getElement());
        paint();
    }

    public List<Double> getUniqueAngles(double x, double y) {
        List<Double> angles = new LinkedList<Double>();

        // Get all unique points
        Set<Point> points = new HashSet<Point>();
        for (Line line : lines) {
            points.add(line.start);
            points.add(line.end);
        }

        for (Point point : points) {
            Double angle = Math.atan2(point.getY() - y, point.getX() - x);
            angles.add(angle - 0.00001);
            angles.add(angle);
            angles.add(angle + 0.00001);
        }

        return angles;
    }
}