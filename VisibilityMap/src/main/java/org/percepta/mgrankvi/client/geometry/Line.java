package org.percepta.mgrankvi.client.geometry;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Line {

    public Point start;
    public Point end;

    public Line(final Point start, final Point end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return start.toString() + "-" + end.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Line) {
            return toString().equals(obj.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
