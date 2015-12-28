package org.percepta.mgrankvi;

import com.vaadin.ui.Component;
import org.percepta.mgrankvi.client.MyComponentClientRpc;
import org.percepta.mgrankvi.client.MyComponentServerRpc;
import org.percepta.mgrankvi.client.VisibilityMapState;
import org.percepta.mgrankvi.client.geometry.Line;
import org.percepta.mgrankvi.client.geometry.Point;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

// This is the server-side UI component that provides public API 
// for MyComponent
public class VisibilityMap extends com.vaadin.ui.AbstractComponent {

    // To process events from the client, we implement ServerRpc
    private MyComponentServerRpc rpc = new MyComponentServerRpc() {

        @Override
        public void moved(Point point) {
            fireChangeEvent(point);
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

    public void setPoint(Point position) {
        getRpcProxy(MyComponentClientRpc.class).updatePosition(position);
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

    public void setGmMode(boolean gm) {
        getState().gmMode = gm;
    }

    public boolean isGmMode() {
        return getState().gmMode;
    }


    private static final Method POSITION_CHANGE_EVENT;

    static {
        try {
            POSITION_CHANGE_EVENT = PositionChangeListener.class.getDeclaredMethod("positionChanged", new Class[]{PositionChangeEvent.class});
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException("Internal error finding methods in TimeSelector");
        }
    }

    /**
     * Selection event. This event is thrown, when a selection is made.
     */
    public class PositionChangeEvent extends Component.Event {
        private static final long serialVersionUID = 1890057101443553065L;

        private final Point point;

        public PositionChangeEvent(final Component source, Point point) {
            super(source);
            this.point = point;
        }

        public Point getPoint() {
            return point;
        }
    }

    /**
     * Interface for listening for a change fired by a {@link Component}.
     */
    public interface PositionChangeListener extends Serializable {
        public void positionChanged(PositionChangeEvent event);

    }

    /**
     * Adds the change listener.
     *
     * @param listener the Listener to be added.
     */
    public void addPositionChangeListener(final PositionChangeListener listener) {
        addListener(PositionChangeEvent.class, listener, POSITION_CHANGE_EVENT);
    }

    /**
     * Removes the selection listener.
     *
     * @param listener the Listener to be removed.
     */
    public void removePositionChangeListener(final PositionChangeListener listener) {
        removeListener(PositionChangeEvent.class, listener, POSITION_CHANGE_EVENT);
    }

    /**
     * Fires a event to all listeners without any event details.
     */
    public void fireChangeEvent(Point point) {
        fireEvent(new PositionChangeEvent(this, point));
    }
}

