package org.percepta.mgrankvi.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.VConsole;
import elemental.events.KeyboardEvent;
import org.percepta.mgrankvi.client.geometry.Calculations;
import org.percepta.mgrankvi.client.geometry.Direction;
import org.percepta.mgrankvi.client.geometry.Intersect;
import org.percepta.mgrankvi.client.geometry.Line;
import org.percepta.mgrankvi.client.geometry.Point;
import org.percepta.mgrankvi.client.items.Movable;
import org.percepta.mgrankvi.client.items.Paintable;
import org.percepta.mgrankvi.client.utils.DrawUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VisibilityMapWidget extends Composite implements MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOutHandler, KeyDownHandler, KeyUpHandler {

    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;

    public static final int DOT_RADIUS = 2;
    public static final int DEFAULT_SPEED = 3;
    public int SPEED = 3;

    public int UPDATE_SPEED = 10;

    private Timer updateTimer;

    Canvas map, bg;

    // x and y velocity
    private int vX, vY;
    private boolean up = false;
    private boolean down = false;
    private boolean left = false;
    private boolean right = false;

    private boolean multipoint = false;
    private boolean enableDebugPoints = false;
    private boolean gmMode = false;
    private boolean painting = false;

    private int fuzzyRadius = 5;
    private int sightPoints = 5;

    private int touchDistance = fuzzyRadius + DOT_RADIUS + SPEED;

    double multipointStep = DrawUtil.FULL_CIRCLE / sightPoints;

    private Map<Double, Direction> angleDirections = new HashMap<Double, Direction>();

    private List<MoveHandler> moveListener = new LinkedList<MoveHandler>();
    private List<Paintable> hidden = new LinkedList<Paintable>();
    private List<Movable> movables = new LinkedList<Movable>();
    private List<Line> lines = new LinkedList<Line>();

    private Set<HandlerRegistration> gmHandlers = new HashSet<HandlerRegistration>();

    private Movable selected = null;
    private String selectedColour;

    private int gridStepSize = 20;

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
    private boolean mouseMoveEnabled = true;
    private boolean step = false;

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
                DrawUtil.drawGrid(bg.getContext2d(), WIDTH, HEIGHT, "hsl(210, 50%, 75%)", gridStepSize);
            } else {
                DrawUtil.drawGrid(bg.getContext2d(), WIDTH, HEIGHT, "hsl(210, 50%, 25%)", gridStepSize);
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

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
    }

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

            if (gmMode) {
                context.setFillStyle("hsla(60,60%,30%,0.3)");
                context.beginPath();
                context.rect(0, 0, WIDTH, HEIGHT);
                context.closePath();
                context.fill();
                paintHidden(context);
            } else {
                List<List<Intersect>> intersects = new LinkedList<List<Intersect>>();

                // Collect all intersection points from position x,y
                intersects.add(Calculations.getSightPolygons(x, y, lines));

                if (multipoint) {
                    // If using multiple points around center collect all intersections for points @ angle+distance
                    for (double angle = 0; angle < DrawUtil.FULL_CIRCLE; angle += multipointStep) {
                        Direction d = getAngleDirections(angle);
                        intersects.add(Calculations.getSightPolygons(x + d.dx, y + d.dy, lines));
                    }
                }
                // Draw our visibility polygon(s)
                drawPolygons(context, intersects);

                // Using intersection points draw visible wall parts.
                drawVisibleWallSegments(context, intersects);

                // Draw hidden content that only shows when inside a visibility polygon
                // SOURCE_ATOP - The new shape is only drawn where it overlaps the existing canvas content.
                context.save();
                context.setGlobalCompositeOperation(Context2d.Composite.SOURCE_ATOP);

                paintHidden(context);
                context.restore();
            }

            // Draw red dots for center position of sight cones.
            context.setFillStyle("#dd3838");
            context.beginPath();
            context.arc(x, y, DOT_RADIUS, 0, 2 * Math.PI, false);
            context.fill();
            if (multipoint) {
                for (double angle = 0; angle < DrawUtil.FULL_CIRCLE; angle += multipointStep) {
                    Direction d = getAngleDirections(angle);
                    context.beginPath();
                    context.arc(x + d.dx, y + d.dy, DOT_RADIUS, 0, DrawUtil.FULL_CIRCLE, false);
                    context.closePath();
                    context.fill();
                }
            }
        } finally {
            painting = false;
        }
    }

    /**
     * Paint all "hidden" items to canvas.
     *
     * @param context Canvas context2d
     */
    private void paintHidden(Context2d context) {
        for (Paintable p : hidden) {
            p.paint(context);
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
                    context.arc(p.getX(), p.getY(), 3, 0, DrawUtil.FULL_CIRCLE, false);
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
        if (gmMode) {
            if (selected != null) {
                selected.movePosition(event.getRelativeX(map.getElement()), event.getRelativeY(map.getElement()));
                paint();
            }
        } else if (mouseMoveEnabled) {
            x = event.getRelativeX(map.getElement());
            y = event.getRelativeY(map.getElement());
            paint();
            move(new Point(x, y));
        }
    }


    @Override
    public void onMouseDown(MouseDownEvent event) {
        int x = event.getRelativeX(map.getElement());
        int y = event.getRelativeY(map.getElement());

        for (Movable movable : movables) {
            if (movable.pointInObject(x, y)) {
                selected = movable;
                selectedColour = ((Paintable) movable).getColour();
                ((Paintable) movable).setColour("orange");
                paint();
                break;
            }
        }
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        ((Paintable) selected).setColour(selectedColour);
        selected = null;
        paint();
    }

    @Override
    public void onMouseOut(MouseOutEvent event) {
        ((Paintable) selected).setColour(selectedColour);
        selected = null;
        paint();
    }

    private void update() {
        vX = 0;
        vY = 0;
        if (down) vY = SPEED;
        if (up) vY = -SPEED;
        if (left) vX = -SPEED;
        if (right) vX = SPEED;

        if (getDistanceToClosestWall(vX, vY) > touchDistance) {
            if(step) {
                Line step = new Line(new Point(x,y), new Point(x+vX,y+vY));
                for(Line line : lines) {
                    if(Calculations.lineSegmentsIntersect(step, line)){
                        VConsole.log("Intersect " + step + " :: " + line);
                        return;
                    }
                }
            }
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
            for (double angle = 0; angle < DrawUtil.FULL_CIRCLE; angle += multipointStep) {
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

        if (updateTimer == null && !step) {
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
        // Update touch distance on visibility point amount change
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
        multipointStep = DrawUtil.FULL_CIRCLE / sightPoints;
        paint();
    }

    public void clearHidden() {
        hidden.clear();
    }

    public void addHidden(Widget widget) {
        if (widget instanceof Paintable) {
            hidden.add((Paintable) widget);
        }
        if (widget instanceof Movable) {
            movables.add((Movable) widget);
        }
    }

    public void setDrawLines(boolean drawLines) {
        this.drawLines = drawLines;
    }

    public void setGmMode(boolean gmMode) {
        this.gmMode = gmMode;
        if (gmMode) {
            gmHandlers.add(map.addDomHandler(this, MouseDownEvent.getType()));
            gmHandlers.add(map.addDomHandler(this, MouseUpEvent.getType()));
            gmHandlers.add(map.addDomHandler(this, MouseOutEvent.getType()));
        } else {
            for (HandlerRegistration handler : gmHandlers) {
                handler.removeHandler();
            }
        }

    }

    // Connector move handler
    public void addMoveHandler(MoveHandler moveHandler) {
        moveListener.add(moveHandler);
    }

    private void move(Point point) {
        for (MoveHandler handler : moveListener) {
            handler.move(point);
        }
    }

    public void setMouseMoveEnabled(boolean mouseMoveEnabled) {
        this.mouseMoveEnabled = mouseMoveEnabled;
    }

    public void setGridStepSize(int gridStepSize) {
        this.gridStepSize = gridStepSize;
    }

    public void setStep(boolean step) {
        this.step = step;
        if (step) {
            SPEED = gridStepSize;
            x = x - (x % gridStepSize) + gridStepSize / 2;
            y = y - (y % gridStepSize) + gridStepSize / 2;
            paint();
        } else {
            SPEED = DEFAULT_SPEED;
        }
    }

    public void setStartPoint(Point startPoint) {
        if (startPoint != null) {
            x = (int) startPoint.getX();
            y = (int) startPoint.getY();
        }
    }
}