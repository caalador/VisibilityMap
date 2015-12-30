package org.percepta.mgrankvi.demo;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.google.gwt.thirdparty.guava.common.collect.Maps;
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
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.percepta.mgrankvi.ImageToLines;
import org.percepta.mgrankvi.VisibilityMap;
import org.percepta.mgrankvi.client.geometry.Line;
import org.percepta.mgrankvi.client.geometry.Point;
import org.percepta.mgrankvi.item.Dot;
import org.percepta.mgrankvi.item.MovableItem;

import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Theme("demo")
@Title("VisibilityMap Demo")
@SuppressWarnings("serial")
@Push
public class DemoUI extends UI implements Broadcaster.BroadcastListener {

    @Override
    public void receiveBroadcast(final String map, final String id, final Point point) {
        access(new Runnable() {
            @Override
            public void run() {
                if (map.equals(mapParam)) {
                    if (id.equals("lines")) {
                        visibilityMap.setLines(Lists.<Line>newArrayList());
                        addLines(visibilityMap);
                    } else if (id.equals("player") && player == null) {
                        visibilityMap.setPoint(point);
                    } else if (items.containsKey(id)) {
                        Dot dot = items.get(id);
                        dot.setPosition(point);
                        visibilityMap.update();
                    }
                }
            }
        });
    }

    @Override
    public void updatePlayer(final String map, final Dot player) {
        access(new Runnable() {
            @Override
            public void run() {
                if (map.equals(mapParam) && !player.equals(DemoUI.this.player)) {
                    Component byId = visibilityMap.getById(player.getId());
                    if (byId != null) {
                        Dot mapPlayer = (Dot) byId;
                        mapPlayer.setPosition(player.getPosition());
                        visibilityMap.update();
                    } else {
                        Dot mapPlayer = new Dot(player.getSize(), player.getPosition());
                        mapPlayer.setMovable(false);
                        mapPlayer.setColour(player.getColour());
                        mapPlayer.setId(player.getId());
                        visibilityMap.addComponent(mapPlayer);
                    }
                }
            }
        });
    }

    @Override
    public void removePlayer(final String map, final Dot player) {
        access(new Runnable() {
            @Override
            public void run() {
                if (map.equals(mapParam)) {
                    visibilityMap.removeComponent(visibilityMap.getById(player.getId()));
                }
            }
        });
    }

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, widgetset = "org.percepta.mgrankvi.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    @Override
    public void detach() {
        if (player != null)
            Broadcaster.broadcastPlayer(mapParam, player, true);
        Broadcaster.unregister(this);
        super.detach();
    }

    private CheckBox multipoint, debug, drawLines, gm;
    private TextField fuzzy, amount;
    private NativeSelect games;

    // Initialize our new UI visibilityMap
    private VisibilityMap visibilityMap;

    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;
    Dot p1, p2, p3;
    final Random rand = new Random(System.currentTimeMillis());

    Map<String, Dot> items = Maps.newHashMap();
    static Map<String, List<Line>> mapLines = Maps.newHashMap();
    static Map<String, Point> points = Maps.newHashMap();
    String mapParam = "empty";

    Dot player;

    @Override
    protected void init(VaadinRequest request) {
        if (request.getParameter("map") != null) {
            mapParam = request.getParameter("map");
            Broadcaster.register(this);
        }
        Boolean gmParam = Boolean.valueOf(request.getParameter("gm"));
        visibilityMap = new VisibilityMap();
        visibilityMap.setMouseMoveEnabled(false);
        if (gmParam) {
            visibilityMap.setGmMode(true);
        } else if (request.getParameter("player") != null) {
            player = new Dot(2, new Point(WIDTH / 2, HEIGHT / 2));
            player.setColour(Colours.cssColours[Math.abs(rand.nextInt() % Colours.cssColours.length)]);
            player.setId(request.getParameter("player") + "-" + mapParam);
        }

        init();
        addLines(visibilityMap);

        if (!gmParam && !mapParam.equals("empty")) {
            visibilityMap.addPositionChangeListener(new VisibilityMap.PositionChangeListener() {
                @Override
                public void positionChanged(VisibilityMap.PositionChangeEvent event) {
                    if (player != null) {
                        player.setPosition(event.getPoint());
                        Broadcaster.broadcastPlayer(mapParam, player, false);
                    } else {
                        Broadcaster.broadcast(mapParam, "player", event.getPoint());
                    }
                }
            });
        }

        initPoints();
        if (gmParam) {
            MovableItem.PositionChangeListener listener = new MovableItem.PositionChangeListener() {
                @Override
                public void positionChanged(MovableItem.PositionChangeEvent event) {
                    Broadcaster.broadcast(mapParam, event.getId(), event.getPoint());

                    if (!mapParam.equals("empty")) {
                        points.put(mapParam + event.getId(), event.getPoint());
                    }
                }
            };
            p1.addPositionChangeListener(listener);
            p2.addPositionChangeListener(listener);
            p3.addPositionChangeListener(listener);
            if (!mapParam.equals("empty")) {
                points.put(mapParam + "-p1", p1.getPosition());
                points.put(mapParam + "-p2", p2.getPosition());
                points.put(mapParam + "-p3", p3.getPosition());
            }
        }
        visibilityMap.addComponent(p1);
        visibilityMap.addComponent(p2);
        visibilityMap.addComponent(p3);

        Button random = new Button("Random", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                p1.setPosition(new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT)));
                p2.setPosition(new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT)));
                p3.setPosition(new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT)));
                update();
            }
        });
        Button clear = new Button("Clear", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                visibilityMap.setLines(Lists.<Line>newLinkedList());
            }
        });

        // Show it in the middle of the screen
        final VerticalLayout layout = new VerticalLayout();

        HorizontalLayout hl = new HorizontalLayout(fuzzy, amount, multipoint, debug, drawLines, gm, random, clear, games);
        hl.setSpacing(true);
        layout.addComponent(hl);

        layout.setStyleName("demoContentLayout");
        layout.setSizeFull();
        HorizontalLayout maps = new HorizontalLayout();
        maps.addComponent(visibilityMap);

        layout.addComponent(maps);
        layout.setComponentAlignment(maps, Alignment.MIDDLE_CENTER);
        layout.setExpandRatio(maps, 1f);
        Panel p = new Panel();
        VerticalLayout imageLayout = new VerticalLayout();
        p.setContent(imageLayout);
        p.setWidth("100%");
        p.setHeight("400px");
        imageLayout.addComponent(getMap("Full Map", "/org/percepta/mgrankvi/demo/Dungeon.png", false));
        imageLayout.addComponent(getMap("Base", "/org/percepta/mgrankvi/demo/dungeon/Base.png", false));
        imageLayout.addComponent(getMap("Divider 1", "/org/percepta/mgrankvi/demo/dungeon/divider1.png", true));
        imageLayout.addComponent(getMap("Divider 2", "/org/percepta/mgrankvi/demo/dungeon/divider2.png", true));
        imageLayout.addComponent(getMap("Divider 3", "/org/percepta/mgrankvi/demo/dungeon/divider3.png", true));
        imageLayout.addComponent(getMap("Divider 4", "/org/percepta/mgrankvi/demo/dungeon/divider4.png", true));
        imageLayout.addComponent(getMap("Divider 5", "/org/percepta/mgrankvi/demo/dungeon/divider5.png", true));
        imageLayout.addComponent(getMap("Divider 6", "/org/percepta/mgrankvi/demo/dungeon/divider6.png", true));

        PopupView mapsPopup = new PopupView("Maps", p);
        layout.addComponent(mapsPopup);
        setContent(layout);

        if (mapParam.equals("empty")) {
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
    }

    private void initPoints() {
        Point position = getPoint("p1");
        p1 = new Dot(3, position);
        p1.setId("p1");
        position = getPoint("p2");
        p2 = new Dot(3, position);
        p2.setId("p2");
        position = getPoint("p3");
        p3 = new Dot(3, position);
        p3.setId("p3");
        items.put("p1", p1);
        items.put("p2", p2);
        items.put("p3", p3);
    }

    private Point getPoint(String id) {
        Point position = new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT));
        if (!mapParam.equals("empty") && points.containsKey(mapParam + "-" + id)) {
            position = points.get(mapParam + "-" + id);
        }
        return position;
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
                if (!mapParam.equals("empty")) {
                    List<Line> lineList = mapLines.get(mapParam);
                    if (!add) {
                        lineList.clear();
                    }
                    lineList.addAll(lines);
                    Broadcaster.broadcast(mapParam, "lines", null);
                }
                if (add) {
                    DemoUI.this.visibilityMap.addLines(lines);
                } else {
                    DemoUI.this.visibilityMap.setLines(lines);
                }
            }
        });
        return map;
    }

    private void update() {
        getUI().access(new Runnable() {
            @Override
            public void run() {
                int x = rand.nextInt() % 2;
                int y = rand.nextInt() % 2;
                p1.setPosition(new Point(nextX(x, p1.getPosition()), nextY(y, p1.getPosition())));

                x = rand.nextInt() % 2;
                y = rand.nextInt() % 2;
                p2.setPosition(new Point(nextX(x, p2.getPosition()), nextY(y, p2.getPosition())));

                x = rand.nextInt() % 2;
                y = rand.nextInt() % 2;
                p3.setPosition(new Point(nextX(x, p3.getPosition()), nextY(y, p3.getPosition())));

                visibilityMap.update();
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
        multipoint = newCheckBox("MultiPoint", visibilityMap.isMultiselect());
        debug = newCheckBox("debug", visibilityMap.isDebug());
        drawLines = newCheckBox("Draw hidden Lines", visibilityMap.isDrawLines());
        gm = newCheckBox("GameMaster", visibilityMap.isGmMode());

        fuzzy = newTextField("Fuzzy radius", "" + visibilityMap.getFuzzyRadius());
        amount = newTextField("Sight points", "" + visibilityMap.getSightPoints());

        games = newSelect("Active games", mapLines.keySet().toArray());

        multipoint.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                visibilityMap.setMultipoint(multipoint.getValue());
            }
        });

        debug.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                visibilityMap.setDebugPoints(debug.getValue());
            }
        });
        drawLines.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                visibilityMap.setDrawLines(drawLines.getValue());
            }
        });
        gm.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                visibilityMap.setGmMode(gm.getValue());
            }
        });

        fuzzy.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                visibilityMap.setFuzzyRadius(Integer.parseInt(fuzzy.getValue()));
            }
        });

        amount.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                visibilityMap.setSightPoints(Integer.parseInt(amount.getValue()));
            }
        });

        games.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                mapParam = (String) games.getValue();
                visibilityMap.setLines(Lists.<Line>newArrayList());
                addLines(visibilityMap);
                p1.setPosition(getPoint("p1"));
                p2.setPosition(getPoint("p2"));
                p3.setPosition(getPoint("p3"));
            }
        });
    }

    private void addLines(VisibilityMap map) {
        List<Line> lines;
        if (mapLines.containsKey(mapParam)) {
            lines = mapLines.get(mapParam);
        } else {
            lines = new LinkedList<Line>() {
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
            };
            if (!mapParam.equals("empty"))
                mapLines.put(mapParam, lines);
        }
        map.addLines(lines);
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
