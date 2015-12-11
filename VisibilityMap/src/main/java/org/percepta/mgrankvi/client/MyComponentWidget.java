package org.percepta.mgrankvi.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import org.percepta.mgrankvi.client.geometry.Calculations;
import org.percepta.mgrankvi.client.geometry.Intersect;
import org.percepta.mgrankvi.client.geometry.Line;
import org.percepta.mgrankvi.client.geometry.Point;

import java.util.LinkedList;
import java.util.List;

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

    int x = 0;
    int y = 0;

    public MyComponentWidget() {


        SimplePanel baseContent = new SimplePanel();

        map = Canvas.createIfSupported();
        if (map != null) {
            baseContent.add(map);

            map.setWidth(WIDTH + "px");
            map.setHeight(HEIGHT + "px");

            map.addDomHandler(this, MouseMoveEvent.getType());
            paint();
        }
        initWidget(baseContent);
    }

    private void paint() {
        clearCanvas();
        Context2d context = map.getContext2d();

        // Segment lines
        context.setStrokeStyle("#999");
        for (Line l : lines) {
            context.beginPath();
            context.moveTo(l.start.getX(), l.start.getY());
            context.lineTo(l.end.getX(), l.end.getY());
            context.closePath();
            context.stroke();
        }

        // from center to mouse
//        Line ray = new Line(new Point(WIDTH / 2, HEIGHT / 2), new Point(x, y));

        List<Intersect> intersectList = new LinkedList<Intersect>();
        // 50 rays
        for (double angle = 0; angle < Math.PI * 2; angle += (Math.PI * 2) / 50) {
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);
            Line ray = new Line(new Point(x,y), new Point(x+dx,y+dy));

            Intersect closest = null;
            // Closest intersection
            for (Line l : lines) {
                Intersect i = Calculations.getIntersection(ray, l);
                if (i == null) continue;
                if (closest == null || i.getT1() < closest.getT1()) {
                    closest = i;
                }
            }
            intersectList.add(closest);
        }
//
//        Point intersection = closest.getIntersectionPoint();

        // draw line
        context.setStrokeStyle("#dd3838");
        context.setFillStyle("#dd3838");
        for(Intersect intersect : intersectList) {
                    Point intersection = intersect.getIntersectionPoint();
            context.beginPath();
            context.moveTo(x, y);
            context.lineTo(intersection.getX(), intersection.getY());
            context.closePath();
            context.stroke();

            // mouse
            context.beginPath();
            context.arc(intersection.getX(), intersection.getY(), 4, 0, 2 * Math.PI, false);
            context.closePath();
            context.fill();
        }
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
}