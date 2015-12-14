package org.percepta.mgrankvi.client.geometry;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Calculations {

    // Find intersection of RAY & SEGMENT
    public static Intersect getIntersection(Line ray, Line segment) {
        Parametric r = new Parametric(ray);
        Parametric s = new Parametric(segment);

        if (areParallel(r, s)) return null;


        // SOLVE FOR T1 & T2
        // r_px+r_dx*T1 = s_px+s_dx*T2 && r_py+r_dy*T1 = s_py+s_dy*T2
        // ==> T1 = (s_px+s_dx*T2-r_px)/r_dx = (s_py+s_dy*T2-r_py)/r_dy
        // ==> s_px*r_dy + s_dx*T2*r_dy - r_px*r_dy = s_py*r_dx + s_dy*T2*r_dx - r_py*r_dx
        // ==> T2 = (r_dx*(s_py-r_py) + r_dy*(r_px-s_px))/(s_dx*r_dy - s_dy*r_dx)
        double t2 = (r.dx * (s.py - r.py) + r.dy * (r.px - s.px)) / (s.dx * r.dy - s.dy * r.dx);
        double t1 = (s.px + s.dx * t2 - r.px) / r.dx;

        if (t1 < 0) return null;
        if (t2 < 0 || t2 > 1) return null;

        return new Intersect(new Point(r.px +r.dx *t1, r.py +r.dy *t1), t1);
    }

    private static boolean areParallel(Parametric line1, Parametric line2) {
        double r_mag = Math.sqrt(line1.dx * line1.dx + line1.dy * line1.dy);
        double s_mag = Math.sqrt(line2.dx * line2.dx + line2.dy * line2.dy);
        if (line1.dx / r_mag == line2.dx / s_mag && line1.dy / r_mag == line2.dy / s_mag) {
            return true;
        }
        return false;
    }

    private static class Parametric {
        // Points
        public double px;
        public double py;

        // Directions
        public double dx;
        public double dy;

        public Parametric(Line line) {
            px = line.start.getX();
            py = line.start.getY();
            dx = line.end.getX() - line.start.getX();
            dy = line.end.getY() - line.start.getY();
        }
    }
}
