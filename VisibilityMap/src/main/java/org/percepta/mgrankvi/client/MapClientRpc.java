package org.percepta.mgrankvi.client;

import com.vaadin.shared.communication.ClientRpc;
import org.percepta.mgrankvi.client.geometry.Point;

// ClientRpc is used to pass events from server to client
// For sending information about the changes to component state, use State instead
public interface MapClientRpc extends ClientRpc {

	// Example API: Fire up alert box in client
	void updatePosition(Point position);

	void paint();
}