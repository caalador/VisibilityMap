package org.percepta.mgrankvi.client.paintable;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.Document;
import org.percepta.mgrankvi.client.geometry.Point;
import org.percepta.mgrankvi.client.utils.DrawUtil;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Dot extends Paintable {

    int size = 2;
    Point position;


    public Dot() {
        // dummy element
        setElement(Document.get().createDivElement());
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setPosition(Point point) {
        position = point;
    }

    @Override
    public void paint(Context2d context) {
        if(position == null) return;
        context.beginPath();
        context.arc(position.getX(), position.getY(), size, 0, DrawUtil.FULL_CIRCLE, false);
        context.closePath();
        context.fill();
    }
}
