package org.percepta.mgrankvi.client.geometry;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Point {

    private double x;
    private double y;

    public Point() {

    }

    public Point(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(final double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(final double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "[" + x + " , " + y + "]";
    }

    public void move(final double x, final double y) {
        setX(getX() + x);
        setY(getY() + y);
    }
}
