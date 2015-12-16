package org.percepta.mgrankvi.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.vaadin.client.VConsole;
import elemental.events.KeyboardEvent;
import org.percepta.mgrankvi.client.geometry.Calculations;
import org.percepta.mgrankvi.client.geometry.Intersect;
import org.percepta.mgrankvi.client.geometry.Line;
import org.percepta.mgrankvi.client.geometry.Point;
import org.percepta.mgrankvi.client.utils.DrawUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VisibilityMapWidget extends Composite implements MouseMoveHandler, KeyDownHandler, KeyUpHandler {

    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;

    private static final int DOT_RADIUS = 2;

    Canvas map, bg;
    boolean multipoint = true;

    int fuzzyRadius = 5;
    int sightPoints = 10;

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

        // DashLine
        add(new Line(new Point(400, 200), new Point(400, 215)));
        add(new Line(new Point(400, 220), new Point(400, 235)));
        add(new Line(new Point(400, 240), new Point(400, 255)));
        add(new Line(new Point(400, 260), new Point(400, 275)));
        add(new Line(new Point(400, 280), new Point(400, 295)));
        add(new Line(new Point(400, 300), new Point(400, 310)));
    }};

    int x = WIDTH / 2;
    int y = HEIGHT / 2;

    public VisibilityMapWidget() {


        SimplePanel baseContent = new SimplePanel();
        AbsolutePanel absolute = new AbsolutePanel();
        baseContent.add(absolute);

        baseContent.getElement().getStyle().setBackgroundColor("green");
        absolute.setWidth(WIDTH + "px");
        absolute.setHeight(HEIGHT + "px");

        map = Canvas.createIfSupported();
        if (map != null) {
            bg = Canvas.createIfSupported();
            bg.setWidth(WIDTH + "px");
            bg.setHeight(HEIGHT + "px");
            bg.setCoordinateSpaceWidth(WIDTH);
            bg.setCoordinateSpaceHeight(HEIGHT);
            DrawUtil.drawGrid(bg.getContext2d(), WIDTH, HEIGHT);


            map.setWidth(WIDTH + "px");
            map.setHeight(HEIGHT + "px");

            map.addDomHandler(this, MouseMoveEvent.getType());
            map.addDomHandler(this, KeyDownEvent.getType());
            map.addDomHandler(this, KeyUpEvent.getType());
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
        intersects.add(Calculations.getSightPolygons(x, y, lines));
        if (multipoint) {
            for (double angle = 0; angle < Math.PI * 2; angle += (Math.PI * 2) / sightPoints) {
                double dx = Math.cos(angle) * fuzzyRadius;
                double dy = Math.sin(angle) * fuzzyRadius;
                intersects.add(Calculations.getSightPolygons(x + dx, y + dy, lines));
            }
        }
//        drawPolygon(context, Calculations.getSightPolygons(x, y, lines), "hsla(60, 75%, 60%, 0.2)");//"rgba(255,255,0,0.2)");
        drawPolygons(context, intersects);

        // Segment lines
        context.setStrokeStyle("orange");
        Map<String, LinkedList<Point>> lines = new HashMap<String, LinkedList<Point>>();
        for (List<Intersect> is : intersects) {
            for (Intersect i : is) {
                Line line = i.line;
                if (lines.containsKey(line.toString())) {
                    lines.get(line.toString()).add(i.getIntersectionPoint());
                } else {
                    LinkedList<Point> points = new LinkedList<Point>();
                    points.add(i.getIntersectionPoint());
                    lines.put(line.toString(), points);
                }
            }
        }
        for (Map.Entry<String, LinkedList<Point>> entry : lines.entrySet()) {
            Iterator<Point> points = entry.getValue().iterator();
            context.save();
            context.beginPath();
            context.setLineWidth(2.0);
            Point p = points.next();
            context.moveTo(p.getX(), p.getY());
            while (points.hasNext()) {
                p = points.next();
                context.lineTo(p.getX(), p.getY());
            }
            context.closePath();
            context.stroke();
            context.restore();
        }

        // Test having hidden points in map that only shown when "visible"
        context.setGlobalCompositeOperation(Context2d.Composite.SOURCE_ATOP);
        context.setFillStyle("#000");
        context.beginPath();
        context.arc(50, 50, DOT_RADIUS, 0, 2 * Math.PI, false);
        context.closePath();
        context.fill();

        context.beginPath();
        context.arc(450, 50, DOT_RADIUS, 0, 2 * Math.PI, false);
        context.closePath();
        context.fill();

        context.setGlobalCompositeOperation(Context2d.Composite.SOURCE_OVER);

        // draw line
//        context.setStrokeStyle("#f55");
//        context.setFillStyle("#dd3838");
//        for(List<Intersect> intersectList: intersects)
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
        context.arc(x, y, DOT_RADIUS, 0, 2 * Math.PI, false);
        context.fill();
        if (multipoint) {
            for (double angle = 0; angle < Math.PI * 2; angle += (Math.PI * 2) / sightPoints) {
                double dx = Math.cos(angle) * fuzzyRadius;
                double dy = Math.sin(angle) * fuzzyRadius;
                context.beginPath();
                context.arc(x + dx, y + dy, DOT_RADIUS, 0, 2 * Math.PI, false);
                context.closePath();
                context.fill();
            }
        }
    }

    private void drawPolygons(Context2d context, List<List<Intersect>> intersects) {
        CanvasGradient radialGradient = context.createRadialGradient(x, y, 0, x, y, WIDTH * 0.75);
        radialGradient.addColorStop(0.0, "hsla(60,100%,75%,0.05");
        radialGradient.addColorStop(0.5, "hsla(60,50%,50%,0.03");
        radialGradient.addColorStop(1.0, "hsla(60,60%,30%,0.01");

        for (List<Intersect> polygon : intersects) {
            drawPolygon(context, polygon, radialGradient);
        }
    }

    private void drawPolygon(Context2d context, List<Intersect> intersectList, FillStrokeStyle fillStyle) {
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

    Timer t;

    public static final int SPEED = 3;
    // x and y velocity
    private int vX, vY;
    private boolean up = false;
    boolean down = false;
    boolean left = false;
    boolean right = false;


    private void update() {
        vX = 0;
        vY = 0;
        if (down) vY = SPEED;
        if (up) vY = -SPEED;
        if (left) vX = -SPEED;
        if (right) vX = SPEED;

        if (getDistanceToClosestWall(vX, vY) > (fuzzyRadius+DOT_RADIUS+SPEED)) {//fuzzyRadius + DOT_RADIUS) {
            if (x + vX > 0 && x + vX < WIDTH) {
                x += vX;
            }
            if (y + vY > 0 && y + vY < HEIGHT) {
                y += vY;
            }
        }
        paint();
    }

    private double getDistanceToClosestWall(int vX, int vY) {
        double distance = Double.MAX_VALUE;
        if (multipoint) {
            for (double angle = 0; angle < Math.PI * 2; angle += (Math.PI * 2) / sightPoints) {
                double dx = Math.cos(angle) * fuzzyRadius;
                double dy = Math.sin(angle) * fuzzyRadius;
                double centerX = this.x + dx;
                double centerY = this.y + dy;
                Line ray = new Line(new Point(centerX, centerY), new Point(centerX + vX, centerY + vY));
                double dist = getDistance(ray);
                if (dist < distance) distance = dist;
            }
        } else {

            Line ray = new Line(new Point(x, y), new Point(x + vX, y + vY));
            return getDistance(ray);
//            Intersect closestIntersectForRay = Calculations.getClosestIntersectForRay(ray, lines);
//            Point intersectionPoint = closestIntersectForRay.getIntersectionPoint();
//
//            return Math.sqrt((intersectionPoint.getX() - x) * (intersectionPoint.getX() - x) + (intersectionPoint.getY() - y) * (intersectionPoint.getY() - y));
        }
VConsole.log(" == min " +distance);
        return distance;
    }

    private double getDistance(Line ray) {
        Intersect closestIntersectForRay = Calculations.getClosestIntersectForRay(ray, lines);
        Point intersectionPoint = closestIntersectForRay.getIntersectionPoint();

        return Math.sqrt((intersectionPoint.getX() - x) * (intersectionPoint.getX() - x) + (intersectionPoint.getY() - y) * (intersectionPoint.getY() - y));
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        if (t == null) {
            t = new Timer() {

                @Override
                public void run() {
                    update();
                }
            };
            t.scheduleRepeating(10);
        }

        switch (event.getNativeKeyCode()) {
            case KeyboardEvent.KeyCode.DOWN:
                down = true;
                break;
            case KeyboardEvent.KeyCode.UP:
                up = true;
                break;
            case KeyboardEvent.KeyCode.LEFT:
                left = true;
                break;
            case KeyboardEvent.KeyCode.RIGHT:
                right = true;
                break;
        }
        update();
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {

        switch (event.getNativeKeyCode()) {
            case KeyboardEvent.KeyCode.DOWN:
                down = false;
                break;
            case KeyboardEvent.KeyCode.UP:
                up = false;
                break;
            case KeyboardEvent.KeyCode.LEFT:
                left = false;
                break;
            case KeyboardEvent.KeyCode.RIGHT:
                right = false;
                break;
        }
        if (t != null && !(down || up || left || right)) {
            t.cancel();
            t = null;
        }
    }
}