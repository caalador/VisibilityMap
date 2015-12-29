package org.percepta.mgrankvi.client.items;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.shared.ui.Connect;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
@Connect(org.percepta.mgrankvi.paintables.Dot.class)
public class DotConnector extends MovableConnector {

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
    }
}
