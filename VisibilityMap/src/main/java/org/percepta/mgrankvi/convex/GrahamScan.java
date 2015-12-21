package org.percepta.mgrankvi.convex;

import org.percepta.mgrankvi.client.geometry.Point;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/*
 * Iterator<Point> pointIterator = GrahamScan.getConvexHull(points).iterator();
 * Point first = pointIterator.next();
 * Point prev = first;
 * while(pointIterator.hasNext()) {
 *  Point p = pointIterator.next();
 *  lines.add(new Line(prev, p));
 *  prev = p;
 * }
 * lines.add(new Line(prev, first));
 */

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public final class GrahamScan {

    /**
     * An enum denoting a directional-turn between 3 points (vectors).
     */
    protected static enum Turn {
        CLOCKWISE, COUNTER_CLOCKWISE, COLLINEAR
    }

    /**
     * Returns true iff all points in <code>points</code> are collinear.
     *
     * @param points the list of points.
     * @return true iff all points in <code>points</code> are collinear.
     */
    protected static boolean areAllCollinear(List<Point> points) {

        if (points.size() < 2) {
            return true;
        }

        final Point a = points.get(0);
        final Point b = points.get(1);

        for (int i = 2; i < points.size(); i++) {

            Point c = points.get(i);

            if (getTurn(a, b, c) != Turn.COLLINEAR) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the convex hull of the points created from the list
     * <code>points</code>. Note that the first and last point in the
     * returned <code>List&lt;java.awt.Point&gt;</code> are the same
     * point.
     *
     * @param points the list of points.
     * @return the convex hull of the points created from the list
     * <code>points</code>.
     * @throws IllegalArgumentException if all points are collinear or if there
     *                                  are less than 3 unique points present.
     */
    public static List<Point> getConvexHull(List<Point> points) throws IllegalArgumentException {

        List<Point> sorted = new ArrayList<Point>(getSortedPointSet(points));

        if (sorted.size() < 3) {
            throw new IllegalArgumentException("can only create a convex hull of 3 or more unique points");
        }

        if (areAllCollinear(sorted)) {
            throw new IllegalArgumentException("cannot create a convex hull from collinear points");
        }

        Stack<Point> stack = new Stack<Point>();
        stack.push(sorted.get(0));
        stack.push(sorted.get(1));

        for (int i = 2; i < sorted.size(); i++) {

            Point head = sorted.get(i);
            Point middle = stack.pop();
            Point tail = stack.peek();

            Turn turn = getTurn(tail, middle, head);

            switch (turn) {
                case COUNTER_CLOCKWISE:
                    stack.push(middle);
                    stack.push(head);
                    break;
                case CLOCKWISE:
                    i--;
                    break;
                case COLLINEAR:
                    stack.push(head);
                    break;
            }
        }

        // close the hull
        stack.push(sorted.get(0));

        return new ArrayList<Point>(stack);
    }

    /**
     * Returns the points with the lowest y coordinate. In case more than 1 such
     * point exists, the one with the lowest x coordinate is returned.
     *
     * @param points the list of points to return the lowest point from.
     * @return the points with the lowest y coordinate. In case more than
     * 1 such point exists, the one with the lowest x coordinate
     * is returned.
     */
    protected static Point getLowestPoint(List<Point> points) {

        Point lowest = points.get(0);

        for (int i = 1; i < points.size(); i++) {

            Point temp = points.get(i);

            if (temp.getY() < lowest.getY() || (temp.getY() == lowest.getY() && temp.getX() < lowest.getX())) {
                lowest = temp;
            }
        }

        return lowest;
    }

    /**
     * Returns a sorted set of points from the list <code>points</code>. The
     * set of points are sorted in increasing order of the angle they and the
     * lowest point <tt>P</tt> make with the x-axis. If tow (or more) points
     * form the same angle towards <tt>P</tt>, the one closest to <tt>P</tt>
     * comes first.
     *
     * @param points the list of points to sort.
     * @return a sorted set of points from the list <code>points</code>.
     * @see GrahamScan#getLowestPoint(java.util.List)
     */
    protected static Set<Point> getSortedPointSet(List<Point> points) {

        final Point lowest = getLowestPoint(points);

        TreeSet<Point> set = new TreeSet<Point>(new Comparator<Point>() {
            @Override
            public int compare(Point a, Point b) {

                if (a == b || a.equals(b)) {
                    return 0;
                }

                // use longs to guard against int-underflow
                double thetaA = Math.atan2(a.getY() - lowest.getY(), a.getX() - lowest.getX());
                double thetaB = Math.atan2(b.getY() - lowest.getY(), b.getX() - lowest.getX());

                if (thetaA < thetaB) {
                    return -1;
                } else if (thetaA > thetaB) {
                    return 1;
                } else {
                    // collinear with the 'lowest' point, let the point closest to it come first

                    // use longs to guard against int-over/underflow
                    double distanceA = Math.sqrt(((lowest.getX() - a.getX()) * (lowest.getX() - a.getX())) +
                            ((lowest.getY() - a.getY()) * (lowest.getY() - a.getY())));
                    double distanceB = Math.sqrt(((lowest.getX() - b.getX()) * (lowest.getX() - b.getX())) +
                            ((lowest.getY() - b.getY()) * (lowest.getY() - b.getY())));

                    if (distanceA < distanceB) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        });

        set.addAll(points);

        return set;
    }

    /**
     * Returns the GrahamScan#Turn formed by traversing through the
     * ordered points <code>a</code>, <code>b</code> and <code>c</code>.
     * More specifically, the cross product <tt>C</tt> between the
     * 3 points (vectors) is calculated:
     * <p/>
     * <tt>(b.x-a.x * c.y-a.y) - (b.y-a.y * c.x-a.x)</tt>
     * <p/>
     * and if <tt>C</tt> is less than 0, the turn is CLOCKWISE, if
     * <tt>C</tt> is more than 0, the turn is COUNTER_CLOCKWISE, else
     * the three points are COLLINEAR.
     *
     * @param a the starting point.
     * @param b the second point.
     * @param c the end point.
     * @return the GrahamScan#Turn formed by traversing through the
     * ordered points <code>a</code>, <code>b</code> and
     * <code>c</code>.
     */
    protected static Turn getTurn(Point a, Point b, Point c) {

        // use longs to guard against int-over/underflow
        long crossProduct = (long) (((b.getX() - a.getX()) * (c.getY() - a.getY())) -
                ((b.getY() - a.getY()) * (c.getX() - a.getX())));

        if (crossProduct > 0) {
            return Turn.COUNTER_CLOCKWISE;
        } else if (crossProduct < 0) {
            return Turn.CLOCKWISE;
        } else {
            return Turn.COLLINEAR;
        }
    }
}