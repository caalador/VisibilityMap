package org.percepta.mgrankvi.item;

import org.percepta.mgrankvi.client.geometry.Point;
import org.percepta.mgrankvi.client.items.DotState;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Dot extends MovableItem {

    public Dot() {
        super();
    }

    public Dot(int size, Point position) {
        super();
        getState().size = size;
        getState().position = position;
    }

    @Override
    protected DotState getState() {
        return (DotState) super.getState();
    }

    public void setPosition(Point p) {
        getState().position = p;
    }

    public void setSize(int size) {
        getState().size = size;
    }

    public Point getPosition() {
        return getState().position;
    }

    public void setColour(String colour) {
        getState().colour = colour;
    }

    public void setMovable(boolean movable) {
        getState().movable = movable;
    }

    public int getSize() {
        return getState().size;
    }

    public String getColour() {
        return getState().colour;
    }
}
