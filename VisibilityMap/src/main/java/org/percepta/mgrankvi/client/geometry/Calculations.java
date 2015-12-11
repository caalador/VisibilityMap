package org.percepta.mgrankvi.client.geometry;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Calculations {


    public static Intersect getIntersection(Line ray, Line segment) {
        Parametric r = new Parametric(ray);
        Parametric s = new Parametric(segment);

        if (areParallel(r, s)) return null;

        double t2 = (r.directionX * (s.pointY - r.pointY) + r.directionY * (r.pointX - s.pointX)) / (s.directionX * r.directionY - s.directionY * r.directionX);
        double t1 = (s.pointX + s.directionX * t2 - r.pointX) / r.directionX;

        if (t1 < 0) return null;
        if (t2 < 0 || t2 > 1) return null;

        return new Intersect(new Point(r.pointX+r.directionX*t1, r.pointY+r.directionY*t1), t1);
    }

    private static boolean areParallel(Parametric r, Parametric s) {
        double r_mag = Math.sqrt(r.directionX * r.directionX + r.directionY * r.directionY);
        double s_mag = Math.sqrt(s.directionX * s.directionX + s.directionY * s.directionY);
        if (r.directionX / r_mag == s.directionX / s_mag && r.directionY / r_mag == s.directionY / s_mag) {
            return true;
        }
        return false;
    }

    private static class Parametric {
        public double pointX;
        public double pointY;
        public double directionX;
        public double directionY;

        public Parametric(Line line) {
            pointX = line.start.getX();
            pointY = line.start.getY();
            directionX = line.end.getX() - line.start.getX();
            directionY = line.end.getY() - line.start.getY();
        }
    }
}
