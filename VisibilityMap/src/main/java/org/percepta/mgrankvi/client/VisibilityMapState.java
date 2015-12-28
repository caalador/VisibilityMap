package org.percepta.mgrankvi.client;

import org.percepta.mgrankvi.client.geometry.Line;
import org.percepta.mgrankvi.client.geometry.Point;

import java.util.LinkedList;
import java.util.List;

public class VisibilityMapState extends com.vaadin.shared.AbstractComponentState {

	// State can have both public variable and bean properties
	public String text = "MyComponent";

	public List<Line> lines = new LinkedList<Line>();
    public List<Point> hidden = new LinkedList<Point>();

	public boolean drawLines = false;
	public boolean multipoint = false;
	public boolean enableDebugPoints = false;

	public int fuzzyRadius = 5;
	public int sightPoints = 5;

    public boolean gmMode = false;
}