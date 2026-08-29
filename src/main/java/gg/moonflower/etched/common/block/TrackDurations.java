package gg.moonflower.etched.common.block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * How long each track runs, in ticks, learned from listeners.
 *
 * <p>A station has to keep its schedule with nobody listening, and nothing tells the server how long a
 * track is. The first listener to hear one through reports it, and from then on the station can time
 * that track itself. Until then it waits to be told the track ended, which is all it could do before.
 *
 * <p>Held in memory only. A restart simply means the next listener teaches it again.
 */
public final class TrackDurations {

    private static final Map<String, Integer> DURATIONS = new ConcurrentHashMap<>();

    /** Shorter than this is a track that failed rather than one that played, so it is not recorded. */
    private static final int MIN_TICKS = 40;

    private TrackDurations() {
    }

    public static void learn(String url, int ticks) {
        if (url != null && ticks >= MIN_TICKS) {
            DURATIONS.put(url, ticks);
        }
    }

    /** The known length in ticks, or 0 when nobody has heard this track through yet. */
    public static int get(String url) {
        return url == null ? 0 : DURATIONS.getOrDefault(url, 0);
    }
}
