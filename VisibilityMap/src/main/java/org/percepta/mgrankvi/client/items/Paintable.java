package org.percepta.mgrankvi.client.items;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public interface Paintable  {

    void paint(Context2d context);

    String getColour();

    void setColour(String colour);
}
