package org.percepta.mgrankvi.client.geometry;

import com.google.gwt.canvas.dom.client.Context2d;

import java.io.Serializable;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Point implements Comparable<Point>, Serializable {

    public double x;
    public double y;

    public Point() {
    }

    public Point(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "[" + x + " , " + y + "]";
    }

    public void move(final double x, final double y) {
        this.x += x;
        this.y += y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point other = (Point) obj;
            return x == other.x && y == other.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public int compareTo(Point o) {
        int x = Double.compare(getX(), o.getX());
        if (x == 0) {
            return Double.compare(getY(), o.getY());
        }
        return x;
    }
}
