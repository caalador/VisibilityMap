package org.percepta.mgrankvi.item;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import org.percepta.mgrankvi.client.MapServerRpc;
import org.percepta.mgrankvi.client.geometry.Point;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public abstract class MovableItem extends AbstractComponent {


    // To process events from the client, we implement ServerRpc
    private MapServerRpc rpc = new MapServerRpc() {

        @Override
        public void moved(Point point) {
            fireChangeEvent(point);
        }
    };

    public MovableItem() {

        // To receive events from the client, we register ServerRpc
        registerRpc(rpc);
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
