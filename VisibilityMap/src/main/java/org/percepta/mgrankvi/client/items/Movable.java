package org.percepta.mgrankvi.client.items;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public interface Movable {

    boolean pointInObject(final double x, final double y);

    void movePosition(final int x, final int y);
}
