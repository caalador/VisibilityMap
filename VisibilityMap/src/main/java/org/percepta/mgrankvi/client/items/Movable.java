package org.percepta.mgrankvi.client.items;

import org.percepta.mgrankvi.client.MoveHandler;
import org.percepta.mgrankvi.client.geometry.Point;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public interface Movable {

    boolean pointInObject(final double x, final double y);

    void movePosition(final int x, final int y);

    void addMoveHandler(MoveHandler moveHandler);

    void move(Point point);
}
