package org.percepta.mgrankvi.client;

import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.communication.ServerRpc;
import org.percepta.mgrankvi.client.geometry.Point;

// ServerRpc is used to pass events from client to server
public interface MapServerRpc extends ServerRpc {

	// Example API: Widget click is clicked
	void moved(Point point);

}
