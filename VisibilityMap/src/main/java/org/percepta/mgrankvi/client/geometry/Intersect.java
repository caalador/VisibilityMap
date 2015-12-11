package org.percepta.mgrankvi.client.geometry;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Intersect {

    Point intersectionPoint;
    Double t1;

    public Intersect(Point intersectionPoint, Double t1) {
        this.intersectionPoint = intersectionPoint;
        this.t1 = t1;
    }

    public Point getIntersectionPoint() {
        return intersectionPoint;
    }

    public Double getT1() {
        return t1;
    }
}
