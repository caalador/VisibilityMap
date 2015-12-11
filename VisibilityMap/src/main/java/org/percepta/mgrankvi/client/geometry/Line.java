package org.percepta.mgrankvi.client.geometry;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Line {

    public Point start;
    public Point end;

    public Line() {
    }

    public Line(final Point start, final Point end) {
        this.start = start;
        this.end = end;
    }

}
