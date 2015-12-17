package org.percepta.mgrankvi.demo;

import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import org.percepta.mgrankvi.VisibilityMap;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.percepta.mgrankvi.client.geometry.Line;
import org.percepta.mgrankvi.client.geometry.Point;

import java.util.LinkedList;

@Theme("demo")
@Title("VisibilityMap Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, widgetset = "org.percepta.mgrankvi.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    private CheckBox multipoint, debug;
    private TextField fuzzy, amount;

    // Initialize our new UI map
    private VisibilityMap map;

    @Override
    protected void init(VaadinRequest request) {

        map = new VisibilityMap();
        init();
        addLines(map);

        // Show it in the middle of the screen
        final VerticalLayout layout = new VerticalLayout();

        HorizontalLayout hl = new HorizontalLayout(multipoint, debug, fuzzy, amount);
        hl.setSpacing(true);
        layout.addComponent(hl);

        layout.setStyleName("demoContentLayout");
        layout.setSizeFull();
        layout.addComponent(map);
        layout.setComponentAlignment(map, Alignment.MIDDLE_CENTER);
        layout.setExpandRatio(map, 1f);
        setContent(layout);

    }
    private void init() {
        multipoint = newCheckBox("MultiPoint", map.isMultiselect());
        debug = newCheckBox("debug", map.isDebug());

        fuzzy= newTextField("Fuzzy radius", ""+map.getFuzzyRadius());
        amount= newTextField("Sight points", ""+map.getSightPoints());

        multipoint.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                map.setMultipoint(multipoint.getValue());
            }
        });

        debug.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                map.setDebugPoints(debug.getValue());
            }
        });

        fuzzy.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                map.setFuzzyRadius(Integer.parseInt(fuzzy.getValue()));
            }
        });

        amount.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                map.setSightPoints(Integer.parseInt(amount.getValue()));
            }
        });

    }

    private void addLines(VisibilityMap map) {

        map.setLines(new LinkedList<Line>(){
            {
                // polygon 1
                add(new Line(new Point(100, 150), new Point(120, 50)));
                add(new Line(new Point(120, 50), new Point(200, 80)));
                add(new Line(new Point(200, 80), new Point(140, 210)));
                add(new Line(new Point(140, 210), new Point(100, 150)));

                // polygon 2
                add(new Line(new Point(100, 200), new Point(120, 250)));
                add(new Line(new Point(120, 250), new Point(60, 300)));
                add(new Line(new Point(60, 300), new Point(100, 200)));

                // Polygon #3
                add(new Line(new Point(200, 260), new Point(220, 150)));
                add(new Line(new Point(220, 150), new Point(300, 200)));
                add(new Line(new Point(300, 200), new Point(350, 320)));
                add(new Line(new Point(350, 320), new Point(200, 260)));

                // Polygon #4
                add(new Line(new Point(340, 60), new Point(360, 40)));
                add(new Line(new Point(360, 40), new Point(370, 70)));
                add(new Line(new Point(370, 70), new Point(340, 60)));

                // Polygon #5
                add(new Line(new Point(450, 190), new Point(560, 170)));
                add(new Line(new Point(560, 170), new Point(540, 270)));
                add(new Line(new Point(540, 270), new Point(430, 290)));
                add(new Line(new Point(430, 290), new Point(450, 190)));

                // Polygon #6
                add(new Line(new Point(400, 95), new Point(580, 50)));
                add(new Line(new Point(580, 50), new Point(480, 150)));
                add(new Line(new Point(480, 150), new Point(400, 95)));

                // DashLine
                add(new Line(new Point(400, 200), new Point(400, 215)));
                add(new Line(new Point(400, 220), new Point(400, 235)));
                add(new Line(new Point(400, 240), new Point(400, 255)));
                add(new Line(new Point(400, 260), new Point(400, 275)));
                add(new Line(new Point(400, 280), new Point(400, 295)));
                add(new Line(new Point(400, 300), new Point(400, 310)));
            }});
    }


    private TextField newTextField(String caption, String value) {
        TextField textField = new TextField(caption);
        textField.setImmediate(true);
        textField.setValue(value);

        return textField;
    }

    private CheckBox newCheckBox(String caption, boolean value) {
        CheckBox checkBox = new CheckBox(caption);
        checkBox.setValue(value);
        checkBox.setImmediate(true);
        return checkBox;
    }

    private NativeSelect newSelect(String caption, Object... values) {
        NativeSelect select = new NativeSelect(caption);

        for(Object item:values) {
            select.addItem(item);
        }

        return select;
    }

}
