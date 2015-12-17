package org.percepta.mgrankvi.client.geometry;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Point implements Comparable<Point> {

    private double x;
    private double y;

    private Point(){}

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
        return super.hashCode();
    }

    @Override
    public int compareTo(Point o) {
        int x = Double.compare(getX(), o.getX());
        if(x == 0) {
            return Double.compare(getY(), o.getY());
        }
        return x;
    }
}
