package org.percepta.mgrankvi.client.items;

import com.vaadin.shared.AbstractComponentState;
import org.percepta.mgrankvi.client.geometry.Point;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class DotState extends AbstractComponentState {

    public Point position = new Point(0,0);
    public int size = 3;
}
