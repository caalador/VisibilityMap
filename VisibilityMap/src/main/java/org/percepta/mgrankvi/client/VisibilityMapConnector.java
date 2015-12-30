package org.percepta.mgrankvi.client;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.VConsole;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.shared.ui.Connect;
import org.percepta.mgrankvi.VisibilityMap;
import org.percepta.mgrankvi.client.geometry.Point;
import org.percepta.mgrankvi.client.items.Paintable;

import java.util.List;

// Connector binds client-side widget class to server-side component class
// Connector lives in the client and the @Connect annotation specifies the
// corresponding server-side component
@Connect(VisibilityMap.class)
public class VisibilityMapConnector extends AbstractHasComponentsConnector {

    // ServerRpc is used to send events to server. Communication implementation
    // is automatically created here
    MapServerRpc rpc = RpcProxy.create(MapServerRpc.class, this);

    public VisibilityMapConnector() {

        // To receive RPC events from server, we register ClientRpc implementation
        registerRpc(MapClientRpc.class, new MapClientRpc() {

            @Override
            public void updatePosition(Point position) {
                getWidget().x = (int) position.getX();
                getWidget().y = (int) position.getY();
                getWidget().paint();
            }

            @Override
            public void paint() {
                getWidget().paint();
            }

        });

        getWidget().addMoveHandler(new MoveHandler() {

            @Override
            public void move(Point point) {
                rpc.moved(point);
            }
        });

    }

    @Override
    public VisibilityMapWidget getWidget() {
        return (VisibilityMapWidget) super.getWidget();
    }

    @Override
    public VisibilityMapState getState() {
        return (VisibilityMapState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        VConsole.log("State changed " + getState().lines.size());
        getWidget().setLines(getState().lines);
//        getWidget().setHidden(getState().hidden);
        getWidget().setWidth(getState().width);
        getWidget().setHeight(getState().height);
    }

    @OnStateChange("multipoint")
    void multipoint() {
        getWidget().setMultipoint(getState().multipoint);
    }

    @OnStateChange("enableDebugPoints")
    void debugPoints() {
        getWidget().setDebugPoints(getState().enableDebugPoints);
    }

    @OnStateChange("fuzzyRadius")
    void fuzzyRadius() {
        getWidget().setFuzzyRadius(getState().fuzzyRadius);
    }

    @OnStateChange("sightPoints")
    void sightPoints() {
        getWidget().setSightPoints(getState().sightPoints);
    }

    @OnStateChange("drawLines")
    void drawLines() {
        getWidget().setDrawLines(getState().drawLines);
    }

    @OnStateChange("gmMode")
    void gmMmode() {
        getWidget().setGmMode(getState().gmMode);
    }

    @OnStateChange("mouseMoveEnabled")
    void setMouseMove() {
        getWidget().setMouseMoveEnabled(getState().mouseMoveEnabled);
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {

        final List<ComponentConnector> children = getChildComponents();
        final VisibilityMapWidget widget = getWidget();
        widget.clearHidden();
        for (final ComponentConnector connector : children) {
            Widget child = connector.getWidget();
            if (child instanceof Paintable) {
                widget.addHidden(child);
            }
        }
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
        // NOOP
    }
}
