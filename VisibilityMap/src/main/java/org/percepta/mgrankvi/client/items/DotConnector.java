package org.percepta.mgrankvi.client.items;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;
import org.percepta.mgrankvi.client.MapClientRpc;
import org.percepta.mgrankvi.client.MapServerRpc;
import org.percepta.mgrankvi.client.MoveHandler;
import org.percepta.mgrankvi.client.geometry.Point;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
@Connect(org.percepta.mgrankvi.item.Dot.class)
public class DotConnector extends AbstractComponentConnector {

    MapServerRpc rpc = RpcProxy.create(MapServerRpc.class, this);

    public DotConnector() {
        getWidget().addMoveHandler(new MoveHandler() {

            @Override
            public void move(Point point) {
                rpc.moved(point);
            }
        });
    }

    @Override
    protected Widget createWidget() {
        return GWT.create(Dot.class);
    }

    @Override
    public Dot getWidget() {
        return (Dot) super.getWidget();
    }

    @Override
    public DotState getState() {
        return (DotState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        getWidget().setPosition(getState().position);
        getWidget().setSize(getState().size);
        getWidget().setColour(getState().colour);
        getWidget().setMovable(getState().movable);
    }
}
