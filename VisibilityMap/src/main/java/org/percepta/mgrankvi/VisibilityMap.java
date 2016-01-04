package org.percepta.mgrankvi;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import org.percepta.mgrankvi.client.MapClientRpc;
import org.percepta.mgrankvi.client.MapServerRpc;
import org.percepta.mgrankvi.client.VisibilityMapState;
import org.percepta.mgrankvi.client.geometry.Line;
import org.percepta.mgrankvi.client.geometry.Point;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

// This is the server-side UI component that provides public API 
// for MyComponent
public class VisibilityMap extends AbstractComponentContainer implements HasComponents {

    // To process events from the client, we implement ServerRpc
    private MapServerRpc rpc = new MapServerRpc() {

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

    /**
     * Set map lines.
     * <p/>
     * Note! Clears all old lines.
     *
     * @param lines Lines to set to map
     */
    public void setLines(List<Line> lines) {
        getState().lines = lines;
    }

    /**
     * Add new map lines.
     *
     * @param lines Lines to add to current map
     */
    public void addLines(List<Line> lines) {
        getState().lines.addAll(lines);
    }

    /**
     * Set if debug points (points that rays and lines intercept @) should be visible
     *
     * @param debugPoints true/false
     */
    public void setDebugPoints(boolean debugPoints) {
        getState().enableDebugPoints = debugPoints;
    }


    /**
     * Set if all lines should be drawn even if hidden from view
     *
     * @param draw true/false
     */
    public void setDrawLines(boolean draw) {
        getState().drawLines = draw;
    }

    /**
     * Set GameMaster mode.
     * This contains functions:
     * - Point is not movable
     * - No visibility polygon
     * - Hidden items that are movable can be moved
     * - All lines are drawn
     *
     * @param gm true/false
     */
    public void setGmMode(boolean gm) {
        getState().gmMode = gm;
    }

    /**
     * Enable or disable moving point position with a mouseMoveEvent
     *
     * @param enabled true/false
     */
    public void setMouseMoveEnabled(boolean enabled) {
        getState().mouseMoveEnabled = enabled;
    }

    /**
     * Set if visibility point should use multiple visibility points for fuzzy corner visibility.
     *
     * @param multipoint true/false
     */
    public void setMultipoint(boolean multipoint) {
        getState().multipoint = multipoint;
    }

    /**
     * Set point distance from center point in multipoint mode.
     *
     * @param fuzzyRadius distance Default: 5
     */
    public void setFuzzyRadius(int fuzzyRadius) {
        getState().fuzzyRadius = fuzzyRadius;
    }

    /**
     * Set amount of sight points to be used in mulipoint mode
     *
     * @param sightPoints sight points, Default: 5
     */
    public void setSightPoints(int sightPoints) {
        getState().sightPoints = sightPoints;
    }

    public boolean isDebug() {
        return getState().enableDebugPoints;
    }

    public boolean isDrawLines() {
        return getState().drawLines;
    }

    public boolean isGmMode() {
        return getState().gmMode;
    }

    public boolean isMultiselect() {
        return getState().multipoint;
    }

    public int getFuzzyRadius() {
        return getState().fuzzyRadius;
    }

    public int getSightPoints() {
        return getState().sightPoints;
    }

    /**
     * Request repaint of map
     */
    public void update() {
        getRpcProxy(MapClientRpc.class).paint();
    }

    /**
     * Set point position on map
     *
     * @param position position Point
     */
    public void setPoint(Point position) {
        getRpcProxy(MapClientRpc.class).updatePosition(position);
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
     * Selection event. This event is thrown, when the default point is moved.
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

    List<Component> children = Lists.newLinkedList();


    @Override
    public void addComponent(final Component c) {
        if (c == null) return;
        children.add(c);
        super.addComponent(c);
        update();
    }

    @Override
    public void removeComponent(final Component c) {
        if (c == null) return;
        children.remove(c);
        super.removeComponent(c);
        markAsDirty();
        update();
    }

    @Override
    public void replaceComponent(final Component oldComponent, final Component newComponent) {
        if (newComponent == null) return;
        final int index = children.indexOf(oldComponent);
        if (index != -1) {
            children.remove(index);
            children.add(index, newComponent);
            fireComponentDetachEvent(oldComponent);
            fireComponentAttachEvent(newComponent);
            update();
        }
    }

    @Override
    public int getComponentCount() {
        return children.size();
    }

    @Override
    public Iterator<Component> iterator() {
        return children.iterator();
    }

    /**
     * Get child component with id if one has been connected to component
     *
     * @param id String component id
     * @return Component or Null if not found.
     */
    public Component getById(String id) {
        for (Component c : children) {
            if (c.getId() != null && c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }
}

