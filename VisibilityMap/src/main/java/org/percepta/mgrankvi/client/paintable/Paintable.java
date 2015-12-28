package org.percepta.mgrankvi.client.paintable;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public abstract class Paintable extends Widget {

    public abstract void paint(Context2d context);
}
