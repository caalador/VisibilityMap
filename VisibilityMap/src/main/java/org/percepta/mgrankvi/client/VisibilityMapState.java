package org.percepta.mgrankvi.client;

import com.vaadin.shared.ui.AbstractComponentContainerState;
import org.percepta.mgrankvi.client.geometry.Line;
import org.percepta.mgrankvi.client.geometry.Point;

import java.util.LinkedList;
import java.util.List;

public class VisibilityMapState extends AbstractComponentContainerState {

    public List<Line> lines = new LinkedList<Line>();
//    public List<Point> hidden = new LinkedList<Point>();

    public boolean drawLines = false;
    public boolean multipoint = false;
    public boolean enableDebugPoints = false;

    public int fuzzyRadius = 5;
    public int sightPoints = 5;

    public boolean gmMode = false;
    public boolean mouseMoveEnabled = true;

    public int gridStepSize = 20;

    public Point startPoint = null;
    public boolean step = false;
}