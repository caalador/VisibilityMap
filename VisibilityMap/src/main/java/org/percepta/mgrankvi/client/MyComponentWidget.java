package org.percepta.mgrankvi.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
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

        // mouse
        context.setFillStyle("#dd3838");
        context.beginPath();
        context.arc(x, y, 4, 0, 2 * Math.PI, false);
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
}