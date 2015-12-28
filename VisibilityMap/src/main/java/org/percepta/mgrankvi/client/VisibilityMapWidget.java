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
import elemental.events.KeyboardEvent;
import org.percepta.mgrankvi.client.geometry.Calculations;
import org.percepta.mgrankvi.client.geometry.Direction;
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

    public static final int DOT_RADIUS = 2;
    public static final double FULL_CIRCLE = 2 * Math.PI;
    public int UPDATE_SPEED = 10;

    private Timer updateTimer;

    public static final int SPEED = 3;
    // x and y velocity
    private int vX, vY;
    private boolean up = false;
    boolean down = false;
    boolean left = false;
    boolean right = false;

    List<MoveHandler> moveListener = new LinkedList<MoveHandler>();

    Canvas map, bg;
    boolean multipoint = false;
    boolean enableDebugPoints = false;
    boolean gmMode = false;

    int fuzzyRadius = 5;
    int sightPoints = 5;

    double multipointStep = FULL_CIRCLE / sightPoints;
    Map<Double, Direction> angleDirections = new HashMap<Double, Direction>();

    List<Point> hidden = new LinkedList<Point>();
    List<Line> lines = new LinkedList<Line>();
    private final List<Line> borderLines = new LinkedList<Line>() {{
        // Border
        add(new Line(new Point(0, 0), new Point(WIDTH, 0)));
        add(new Line(new Point(WIDTH, 0), new Point(WIDTH, HEIGHT)));
        add(new Line(new Point(WIDTH, HEIGHT), new Point(0, HEIGHT)));
        add(new Line(new Point(0, HEIGHT), new Point(0, 0)));
    }};

    int x = WIDTH / 2;
    int y = HEIGHT / 2;
    private boolean drawLines = false;

    public VisibilityMapWidget() {
        lines.addAll(borderLines);

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
            if (gmMode) {
                DrawUtil.drawGrid(bg.getContext2d(), WIDTH, HEIGHT, "hsl(210, 50%, 75%)");
            } else {
                DrawUtil.drawGrid(bg.getContext2d(), WIDTH, HEIGHT, "hsl(210, 50%, 25%)");
            }


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

    boolean painting = false;

    protected void paint() {
        if (painting) return;
        painting = true;
        try {
            clearCanvas();
            Context2d context = map.getContext2d();

            if (drawLines || gmMode) {
                // Segment lines
                context.setStrokeStyle("#999");
                for (Line l : lines) {
                    context.beginPath();
                    context.moveTo(l.start.getX(), l.start.getY());
                    context.lineTo(l.end.getX(), l.end.getY());
                    context.closePath();
                    context.stroke();
                }

            }

            if (!gmMode) {
                List<List<Intersect>> intersects = new LinkedList<List<Intersect>>();

                // Collect all intersection points from position x,y
                intersects.add(Calculations.getSightPolygons(x, y, lines));

                if (multipoint) {
                    // If using multiple points around center collect all intersections for points @ angle+distance
                    for (double angle = 0; angle < FULL_CIRCLE; angle += multipointStep) {
                        Direction d = getAngleDirections(angle);
                        intersects.add(Calculations.getSightPolygons(x + d.dx, y + d.dy, lines));
                    }
                }
                // Draw our visibility polygon(s)
                drawPolygons(context, intersects);

                // Using intersection points draw visible wall parts.
                drawVisibleWallSegments(context, intersects);

                // Draw hidden content that only shows when inside a visibility polygon
                context.save();
                context.setGlobalCompositeOperation(Context2d.Composite.SOURCE_ATOP);

                paintHidden(context);
                context.restore();
            } else {
                context.setFillStyle("hsla(60,100%,75%,0.5)");
                context.beginPath();
                context.rect(0,0,WIDTH,HEIGHT);
                context.closePath();
                context.fill();
                paintHidden(context);
            }

            // Draw red dots for center position of sight cones.
            context.setFillStyle("#dd3838");
            context.beginPath();
            context.arc(x, y, DOT_RADIUS, 0, 2 * Math.PI, false);
            context.fill();
            if (multipoint) {
                for (double angle = 0; angle < FULL_CIRCLE; angle += multipointStep) {
                    Direction d = getAngleDirections(angle);
                    context.beginPath();
                    context.arc(x + d.dx, y + d.dy, DOT_RADIUS, 0, FULL_CIRCLE, false);
                    context.closePath();
                    context.fill();
                }
            }
        } finally {
            painting = false;
        }
    }

    private void paintHidden(Context2d context) {
        context.setFillStyle("#000");
        for (Point p : hidden) {
            context.beginPath();
            context.arc(p.getX(), p.getY(), 5, 0, FULL_CIRCLE, false);
            context.closePath();
            context.fill();
        }
    }

    /**
     * Using the given intersection points draw the visible parts of all the
     * visible walls.
     *
     * @param context    context to draw to
     * @param intersects intersects to use
     */
    private void drawVisibleWallSegments(Context2d context, List<List<Intersect>> intersects) {
        // Segment lines
        context.save();
        context.setStrokeStyle("orange");

        for (List<Intersect> list : intersects) {
            context.beginPath();
            context.setLineWidth(2.0);

            Iterator<Intersect> intersect = list.iterator();

            Intersect intersection = intersect.next();
            Intersect initial = intersection;
            Line previousLine = intersection.line;
            context.moveTo(intersection.getX(), intersection.getY());

            while (intersect.hasNext()) {
                intersection = intersect.next();
                if (intersection.line.equals(previousLine)) {
                    context.lineTo(intersection.getX(), intersection.getY());
                } else {
                    context.moveTo(intersection.getX(), intersection.getY());
                }
                previousLine = intersection.line;
            }
            if (initial.line.equals(previousLine)) {
                context.lineTo(initial.getX(), initial.getY());
            } else {
                context.moveTo(initial.getX(), initial.getY());
            }
            context.closePath();
            context.stroke();

            // Enable for debugging purposes to see where we have position points
            if (enableDebugPoints) {
                intersect = list.iterator();

                while (intersect.hasNext()) {
                    Point p = intersect.next().getIntersectionPoint();
                    context.beginPath();
                    context.arc(p.getX(), p.getY(), 3, 0, FULL_CIRCLE, false);
                    context.closePath();
                    context.fill();
                }
            }
        }
        context.restore();
    }

    private void drawPolygons(Context2d context, List<List<Intersect>> intersects) {
        double alpha = multipoint ? 0.5 / sightPoints : 0.5;
        double alpha2 = multipoint ? 0.3 / sightPoints : 0.3;
        double alpha3 = multipoint ? 0.1 / sightPoints : 0.1;

        CanvasGradient radialGradient = context.createRadialGradient(x, y, 0, x, y, WIDTH * 0.75);
        radialGradient.addColorStop(0.0, "hsla(60,100%,75%," + alpha);
        radialGradient.addColorStop(0.5, "hsla(60,50%,50%," + alpha2);
        radialGradient.addColorStop(1.0, "hsla(60,60%,30%," + alpha3);

        for (List<Intersect> polygon : intersects) {
            drawPolygon(context, polygon, radialGradient);
        }
    }

    private void drawPolygon(Context2d context, List<Intersect> intersectList, FillStrokeStyle fillStyle) {
        if (intersectList.isEmpty()) return;
        // Area Polygon
        context.setFillStyle(fillStyle);
        context.beginPath();
        Iterator<Intersect> intersects = intersectList.iterator();
        Intersect intersectionPoint = intersects.next();
        context.moveTo(intersectionPoint.getX(), intersectionPoint.getY());
        while (intersects.hasNext()) {
            intersectionPoint = intersects.next();
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
        if (gmMode) return;

        x = event.getRelativeX(map.getElement());
        y = event.getRelativeY(map.getElement());
        paint();
        move(new Point(x, y));
    }

    private int touchDistance = fuzzyRadius + DOT_RADIUS + SPEED;

    private void update() {
        vX = 0;
        vY = 0;
        if (down) vY = SPEED;
        if (up) vY = -SPEED;
        if (left) vX = -SPEED;
        if (right) vX = SPEED;

        if (getDistanceToClosestWall(vX, vY) > touchDistance) {
            if (x + vX > 0 && x + vX < WIDTH) {
                x += vX;
            }
            if (y + vY > 0 && y + vY < HEIGHT) {
                y += vY;
            }
        }
        paint();
        move(new Point(x, y));
    }

    private double getDistanceToClosestWall(int vX, int vY) {
        double distance = Double.MAX_VALUE;
        if (multipoint) {
            for (double angle = 0; angle < FULL_CIRCLE; angle += multipointStep) {
                Direction d = getAngleDirections(angle);
                double centerX = x + d.dx;
                double centerY = y + d.dy;
                Line ray = new Line(new Point(centerX, centerY), new Point(centerX + vX, centerY + vY));
                double dist = getDistance(ray);
                if (dist < distance) distance = dist;
            }
        } else {
            Line ray = new Line(new Point(x, y), new Point(x + vX, y + vY));
            return getDistance(ray);
        }
        return distance;
    }

    private double getDistance(Line ray) {
        Intersect closestIntersectForRay = Calculations.getClosestIntersectForRay(ray, lines);
        Point intersectionPoint = closestIntersectForRay.getIntersectionPoint();

        return Math.sqrt((intersectionPoint.getX() - x) * (intersectionPoint.getX() - x) + (intersectionPoint.getY() - y) * (intersectionPoint.getY() - y));
    }

    private Direction getAngleDirections(double angle) {
        if (angleDirections.containsKey(angle)) {
            return angleDirections.get(angle);
        }
        Direction d = new Direction();
        d.dx = Math.cos(angle) * fuzzyRadius;
        d.dy = Math.sin(angle) * fuzzyRadius;

        angleDirections.put(angle, d);

        return d;
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        if (gmMode) return;

        if (updateTimer == null) {
            updateTimer = new Timer() {

                @Override
                public void run() {
                    update();
                }
            };
            updateTimer.scheduleRepeating(UPDATE_SPEED);
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
        if (gmMode) return;

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
        if (updateTimer != null && !(down || up || left || right)) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    public void setLines(List<Line> lines) {
        List<Line> clear = new LinkedList<Line>();

        for (Line line : this.lines) {
            if (!lines.contains(line) || !borderLines.contains(line)) {
                clear.add(line);
            }
        }
        this.lines.removeAll(clear);
        this.lines.addAll(borderLines);
        this.lines.addAll(lines);

        paint();
    }

    public void setMultipoint(boolean multipoint) {
        this.multipoint = multipoint;
        touchDistance = multipoint ? fuzzyRadius + DOT_RADIUS + SPEED : DOT_RADIUS + SPEED;
        paint();
    }

    public void setDebugPoints(boolean debugPoints) {
        enableDebugPoints = debugPoints;
        paint();
    }

    public void setFuzzyRadius(int fuzzyRadius) {
        this.fuzzyRadius = fuzzyRadius;
        if (multipoint)
            touchDistance = fuzzyRadius + DOT_RADIUS + SPEED;
        angleDirections.clear();
        paint();
    }

    public void setSightPoints(int sightPoints) {
        this.sightPoints = sightPoints;
        multipointStep = FULL_CIRCLE / sightPoints;
        paint();
    }

    public void setHidden(List<Point> hidden) {
        List<Point> clear = new LinkedList<Point>();

        for (Point point : this.hidden) {
            if (!hidden.contains(point)) {
                clear.add(point);
            }
        }
        this.hidden.removeAll(clear);
        this.hidden.addAll(hidden);
        paint();
    }

    public void setDrawLines(boolean drawLines) {
        this.drawLines = drawLines;
    }

    public void setGmMode(boolean gmMode) {
        this.gmMode = gmMode;
    }

    public void addMoveHandler(MoveHandler moveHandler) {
        moveListener.add(moveHandler);
    }

    private void move(Point point) {
        for (MoveHandler handler : moveListener) {
            handler.move(point);
        }
    }
}