package org.percepta.mgrankvi;

import com.vaadin.shared.MouseEventDetails;
import org.percepta.mgrankvi.client.MyComponentClientRpc;
import org.percepta.mgrankvi.client.MyComponentServerRpc;
import org.percepta.mgrankvi.client.VisibilityMapState;
import org.percepta.mgrankvi.client.geometry.Line;
import org.percepta.mgrankvi.client.geometry.Point;

import java.util.List;

// This is the server-side UI component that provides public API 
// for MyComponent
public class VisibilityMap extends com.vaadin.ui.AbstractComponent {

    private int clickCount = 0;

    // To process events from the client, we implement ServerRpc
    private MyComponentServerRpc rpc = new MyComponentServerRpc() {

        // Event received from client - user clicked our widget
        public void clicked(MouseEventDetails mouseDetails) {

            // Send nag message every 5:th click with ClientRpc
            if (++clickCount % 5 == 0) {
                getRpcProxy(MyComponentClientRpc.class)
                        .alert("Ok, that's enough!");
            }

            // Update shared state. This state update is automatically
            // sent to the client.
            getState().text = "You have clicked " + clickCount + " times";
        }
    };

    public VisibilityMap() {

        // To receive events from the client, we register ServerRpc
        registerRpc(rpc);
    }

    // We must override getState() to cast the state to MyComponentState
    @Override
    protected VisibilityMapState getState() {
        return (VisibilityMapState) super.getState();
    }

    public void setLines(List<Line> lines) {
        getState().lines = lines;
    }

    public void addLines(List<Line> lines) {
        getState().lines.addAll(lines);
    }

    public void setMultipoint(boolean multipoint) {
        getState().multipoint = multipoint;
    }

    public void setDebugPoints(boolean debugPoints) {
        getState().enableDebugPoints = debugPoints;
    }

    public void setFuzzyRadius(int fuzzyRadius) {
        getState().fuzzyRadius = fuzzyRadius;
    }

    public void setSightPoints(int sightPoints) {
        getState().sightPoints = sightPoints;
    }

    public boolean isMultiselect() {
        return getState().multipoint;
    }

    public boolean isDebug() {
        return getState().enableDebugPoints;
    }

    public int getFuzzyRadius() {
        return getState().fuzzyRadius;
    }

    public int getSightPoints() {
        return getState().sightPoints;
    }

    public void addHidden(Point hidden) {
        getState().hidden.add(hidden);
    }

    public void clearHidden() {
        getState().hidden.clear();
    }

    public void setDrawLines(boolean draw) {
        getState().drawLines = draw;
    }

    public boolean isDrawLines() {
        return getState().drawLines;
    }
}

