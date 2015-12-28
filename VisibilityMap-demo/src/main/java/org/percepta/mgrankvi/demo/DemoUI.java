package org.percepta.mgrankvi.demo;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.percepta.mgrankvi.ImageToLines;
import org.percepta.mgrankvi.VisibilityMap;
import org.percepta.mgrankvi.client.geometry.Line;
import org.percepta.mgrankvi.client.geometry.Point;

import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Theme("demo")
@Title("VisibilityMap Demo")
@SuppressWarnings("serial")
@Push
public class DemoUI extends UI {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, widgetset = "org.percepta.mgrankvi.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    private CheckBox multipoint, debug, drawLines, gm;
    private TextField fuzzy, amount;

    // Initialize our new UI map
    private VisibilityMap map;
    private VisibilityMap mapGm;

    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;
    Point p1, p2, p3;
    final Random rand = new Random(System.currentTimeMillis());

    @Override
    protected void init(VaadinRequest request) {

        map = new VisibilityMap();
        mapGm = new VisibilityMap();
        mapGm.setGmMode(true);

        init();
        addLines(map);
        addLines(mapGm);
        map.addPositionChangeListener(new VisibilityMap.PositionChangeListener() {
            @Override
            public void positionChanged(VisibilityMap.PositionChangeEvent event) {
                mapGm.setPoint(event.getPoint());
            }
        });

        p1 = new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT));
        p2 = new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT));
        p3 = new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT));
        map.addHidden(p1);
        map.addHidden(p2);
        map.addHidden(p3);
        mapGm.addHidden(p1);
        mapGm.addHidden(p2);
        mapGm.addHidden(p3);

        Button random = new Button("Random", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                map.clearHidden();

                p1 = new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT));
                p2 = new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT));
                p3 = new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT));
                map.addHidden(p1);
                map.addHidden(p2);
                map.addHidden(p3);
                mapGm.addHidden(p1);
                mapGm.addHidden(p2);
                mapGm.addHidden(p3);
            }
        });
        Button clear = new Button("Clear", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                map.setLines(Lists.<Line>newLinkedList());
            }
        });

        // Show it in the middle of the screen
        final VerticalLayout layout = new VerticalLayout();

        HorizontalLayout hl = new HorizontalLayout(fuzzy, amount, multipoint, debug, drawLines, gm, random, clear);
        hl.setSpacing(true);
        layout.addComponent(hl);

        layout.setStyleName("demoContentLayout");
        layout.setSizeFull();
        HorizontalLayout maps = new HorizontalLayout();
        maps.addComponent(map);
        maps.addComponent(mapGm);

        layout.addComponent(maps);
        layout.setComponentAlignment(maps, Alignment.MIDDLE_CENTER);
        layout.setExpandRatio(maps, 1f);
        Panel p = new Panel();
        VerticalLayout imageLayout = new VerticalLayout();
        p.setContent(imageLayout);
        p.setWidth("100%");
        p.setHeight("400px");
        imageLayout.addComponent(getMap("Full Map", "/Users/Mikael/Desktop/dungeon/Dungeon2.png", false));// "/org/percepta/mgrankvi/demo/Dungeon.png", false));
        imageLayout.addComponent(getMap("Base", "/org/percepta/mgrankvi/demo/dungeon/Base.png", false));
        imageLayout.addComponent(getMap("Divider 1", "/org/percepta/mgrankvi/demo/dungeon/divider1.png", true));
        imageLayout.addComponent(getMap("Divider 2", "/org/percepta/mgrankvi/demo/dungeon/divider2.png", true));
        imageLayout.addComponent(getMap("Divider 3", "/org/percepta/mgrankvi/demo/dungeon/divider3.png", true));
        imageLayout.addComponent(getMap("Divider 4", "/org/percepta/mgrankvi/demo/dungeon/divider4.png", true));
        imageLayout.addComponent(getMap("Divider 5", "/org/percepta/mgrankvi/demo/dungeon/divider5.png", true));
        imageLayout.addComponent(getMap("Divider 6", "/org/percepta/mgrankvi/demo/dungeon/divider6.png", true));
        layout.addComponent(p);
        setContent(layout);

        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 1500, 500);
        getUI().addDetachListener(new DetachListener() {
            @Override
            public void detach(DetachEvent detachEvent) {
                timer.cancel();
            }
        });
    }

    private Image getMap(String caption, final String file, final boolean add) {
        File sourceFile;
        if (getClass().getResource(file) != null) {
            sourceFile = new File(getClass().getResource(file).getFile());
        } else {
            sourceFile = new File(file);
        }
        Image map = new Image(caption, new FileResource(sourceFile));
        map.addClickListener(new MouseEvents.ClickListener() {
            @Override
            public void click(MouseEvents.ClickEvent clickEvent) {
                List<Line> lines = new ImageToLines().getLines(file);
                if (add) {
                    DemoUI.this.map.addLines(lines);
                    DemoUI.this.mapGm.addLines(lines);
                }else {
                    DemoUI.this.map.setLines(lines);
                    DemoUI.this.mapGm.setLines(lines);
                }
            }
        });
        return map;
    }

    private void update() {
        getUI().access(new Runnable() {
            @Override
            public void run() {
                map.clearHidden();
                mapGm.clearHidden();
                int x = rand.nextInt() % 2;
                int y = rand.nextInt() % 2;
                p1 = new Point(nextX(x, p1), nextY(y, p1));

                x = rand.nextInt() % 2;
                y = rand.nextInt() % 2;
                p2 = new Point(nextX(x, p2), nextY(y, p2));

                x = rand.nextInt() % 2;
                y = rand.nextInt() % 2;
                p3 = new Point(nextX(x, p3), nextY(y, p3));
                map.addHidden(p1);
                map.addHidden(p2);
                map.addHidden(p3);
                mapGm.addHidden(p1);
                mapGm.addHidden(p2);
                mapGm.addHidden(p3);
            }
        });
    }

    public double nextX(int x, Point p) {
        if (x == 0 && p.getX() + 1 < WIDTH) {
            return p.getX() + 1;
        }
        return p.getX() - 1;
    }

    public double nextY(int y, Point p) {
        if (y == 0 && p.getY() + 1 < WIDTH) {
            return p.getY() + 1;
        }
        return p.getY() - 1;
    }

    private void init() {
        multipoint = newCheckBox("MultiPoint", map.isMultiselect());
        debug = newCheckBox("debug", map.isDebug());
        drawLines = newCheckBox("Draw hidden Lines", map.isDrawLines());
        gm = newCheckBox("GameMaster", map.isGmMode());

        fuzzy = newTextField("Fuzzy radius", "" + map.getFuzzyRadius());
        amount = newTextField("Sight points", "" + map.getSightPoints());

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
        drawLines.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                map.setDrawLines(drawLines.getValue());
            }
        });
        gm.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                map.setGmMode(gm.getValue());
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
        List<Line> lines = Lists.newLinkedList();
//        List<HarrisFast.Corner> corners = getCorners(getImage("/Users/Mikael/Desktop/dungeon/Base.png"));
//        lines.addAll(getLines(corners));
//        corners = getCorners(getImage("/Users/Mikael/Desktop/dungeon/divider1.png"));
//        lines.addAll(getLines(corners));
//        corners = getCorners(getImage("/Users/Mikael/Desktop/dungeon/divider2.png"));
//        lines.addAll(getLines(corners));
//        corners = getCorners(getImage("/Users/Mikael/Desktop/dungeon/divider3.png"));
//        lines.addAll(getLines(corners));
//        corners = getCorners(getImage("/Users/Mikael/Desktop/dungeon/divider4.png"));
//        lines.addAll(getLines(corners));
//        corners = getCorners(getImage("/Users/Mikael/Desktop/dungeon/divider5.png"));
//        lines.addAll(getLines(corners));
//        List<HarrisFast.Corner>corners = getCorners(getImage("/Users/Mikael/Desktop/dungeon/divider6.png"));
//        lines.addAll(getLines("/Users/Mikael/Desktop/dungeon/Base.png"));
//        lines.addAll(getLines("/Users/Mikael/Desktop/dungeon/divider1.png"));
//        lines.addAll(getLines("/Users/Mikael/Desktop/dungeon/divider2.png"));
//        lines.addAll(getLines("/Users/Mikael/Desktop/dungeon/divider3.png"));
//        lines.addAll(getLines("/Users/Mikael/Desktop/dungeon/divider4.png"));
//        lines.addAll(getLines("/Users/Mikael/Desktop/dungeon/divider5.png"));
//        lines.addAll(getLines("/Users/Mikael/Desktop/dungeon/divider6.png"));
//        lines.addAll(new ImageToLines().getLines("/org/percepta/mgrankvi/demo/Dungeon.png"));//"/Users/Mikael/Desktop/dungeon/Dungeon.png"));
//        System.out.println(lines.size());
//
//        map.setLines(lines);
        map.setLines(new LinkedList<Line>() {
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
            }
        });
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

        for (Object item : values) {
            select.addItem(item);
        }

        return select;
    }

}
