package org.percepta.mgrankvi.paintables;

import com.vaadin.ui.AbstractComponent;
import org.percepta.mgrankvi.client.geometry.Point;
import org.percepta.mgrankvi.client.items.DotState;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Dot extends AbstractComponent {

    public Dot() {}

    public Dot(int size, Point position) {
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

}
