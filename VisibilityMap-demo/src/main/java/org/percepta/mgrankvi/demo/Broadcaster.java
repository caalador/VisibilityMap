package org.percepta.mgrankvi.demo;

import org.percepta.mgrankvi.client.geometry.Point;
import org.percepta.mgrankvi.item.Dot;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Broadcaster implements Serializable {
    static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public interface BroadcastListener {
        void updatePoint(String map, String id, Point point);

        void updatePlayer(String map, Dot player);

        void removePlayer(String map, Dot player);
    }

    private static LinkedList<BroadcastListener> listeners = new LinkedList<BroadcastListener>();

    public static synchronized void register(BroadcastListener listener) {
        listeners.add(listener);
    }

    public static synchronized void unregister(BroadcastListener listener) {
        listeners.remove(listener);
    }

    public static synchronized void broadcastPointUpdate(final String map, final String id, final Point point) {
        for (final BroadcastListener listener : listeners)
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    listener.updatePoint(map, id, point);
                }
            });
    }


    public static synchronized void broadcastPlayer(final String map, final Dot player, final boolean remove) {
        for (final BroadcastListener listener : listeners)
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (remove) listener.removePlayer(map, player);
                    else listener.updatePlayer(map, player);
                }
            });
    }

}