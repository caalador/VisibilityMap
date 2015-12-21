package org.percepta.mgrankvi.convex;

import org.percepta.mgrankvi.client.geometry.Point;

import java.util.Arrays;
import java.util.List;

/*
 * Jarvis j = new Jarvis();
 * List<Point> points = j.convexHull(points.toArray(new Point[points.size()]));
 * Iterator<Point> pointIterator = points.iterator();
 * Point first = pointIterator.next();
 * Point prev = first;
 * while(pointIterator.hasNext()) {
 *  Point p = pointIterator.next();
 *  lines.add(new Line(prev, p));
 *  prev = p;
 * }
 * lines.add(new Line(prev, first));
 */
public class JarvisMarch {
    private boolean CCW(Point p, Point q, Point r) {
        double val = (q.getY() - p.getY()) * (r.getX() - q.getX()) - (q.getX() - p.getX()) * (r.getY() - q.getY());

        if (val >= 0)
            return false;
        return true;
    }

    public List<Point> convexHull(Point[] points) {
        int n = points.length;
        /** if less than 3 points return **/
        if (n < 3)
            return Arrays.asList(points);
        System.out.println("Actually sorting");
        int[] next = new int[n];
        Arrays.fill(next, -1);

        /** find the leftmost point **/
        int leftMost = 0;
        for (int i = 1; i < n; i++)
            if (points[i].getX() < points[leftMost].getX())
                leftMost = i;
        int p = leftMost, q;
        /** iterate till p becomes leftMost **/
        do {
            /** wrapping **/
            q = (p + 1) % n;
            for (int i = 0; i < n; i++)
                if (CCW(points[p], points[i], points[q]))
                    q = i;

            next[p] = q;
            p = q;
        } while (p != leftMost);

        /** Display result **/
//        display(points, next);
        return Arrays.asList(points);
    }

    public void display(Point[] points, int[] next) {
        System.out.println("\nConvex Hull points : ");
        for (int i = 0; i < next.length; i++)
            if (next[i] != -1)
                System.out.println("(" + points[i].getX() + ", " + points[i].getY() + ")");
    }
}