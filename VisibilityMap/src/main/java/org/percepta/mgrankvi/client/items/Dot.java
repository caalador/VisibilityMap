package org.percepta.mgrankvi.client.items;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.VConsole;
import org.percepta.mgrankvi.client.MoveHandler;
import org.percepta.mgrankvi.client.geometry.Point;
import org.percepta.mgrankvi.client.utils.DrawUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Dot extends Widget implements Paintable, Movable {

    int size = 3;
    Point position;
    private String colour;
    private boolean movable;


    public Dot() {
        // dummy element
        setElement(Document.get().createDivElement());
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setPosition(Point point) {
        position = point;
    }

    @Override
    public void paint(Context2d context) {
        if (position == null) return;
        context.setFillStyle(colour);
        context.beginPath();
        context.arc(position.getX(), position.getY(), size, 0, DrawUtil.FULL_CIRCLE, false);
        context.closePath();
        context.fill();
    }

    @Override
    public boolean pointInObject(double x, double y) {
        // Kill moving of object if not movable so we can't even make a selection.
        if(!movable) return false;
        return isInsideCircle(x, y);
    }

    private boolean isInsideCircle(double pointX, double pointY) {
        double relX = pointX - position.x;
        double relY = pointY - position.y;
        return relX * relX + relY * relY <= (size + 1) * (size + 1);
    }

    @Override
    public void movePosition(int x, int y) {
        position.x = x;
        position.y = y;
        move(position);
    }

    List<MoveHandler> moveListener = new LinkedList<MoveHandler>();

    @Override
    public void addMoveHandler(MoveHandler moveHandler) {
        moveListener.add(moveHandler);
    }

    @Override
    public void move(Point point) {
        for (MoveHandler handler : moveListener) {
            handler.move(point);
        }
    }

    @Override
    public void setColour(String colour) {
        this.colour = colour;
    }

    @Override
    public String getColour() {
        return colour;
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
    }
}
