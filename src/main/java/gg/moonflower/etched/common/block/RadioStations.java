package gg.moonflower.etched.common.block;

import gg.moonflower.etched.common.blockentity.RadioStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Every station currently loaded, so a radio can tune to one anywhere in the world rather than only to
 * a station it happens to be near.
 *
 * <p>Only loaded stations are listed, which is the honest set: a station in an unloaded chunk cannot put
 * anything on air either way.
 */
public final class RadioStations {

    private static final Map<ResourceKey<Level>, Set<BlockPos>> STATIONS = new ConcurrentHashMap<>();

    private RadioStations() {
    }

    public static void add(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        STATIONS.computeIfAbsent(level.dimension(), key -> ConcurrentHashMap.newKeySet()).add(pos.immutable());
    }

    public static void remove(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        Set<BlockPos> known = STATIONS.get(level.dimension());
        if (known != null) {
            known.remove(pos);
        }
    }

    /** Stations in this level that still exist, in a stable order so a tuning list does not jump about. */
    public static List<RadioStationBlockEntity> listIn(Level level) {
        Set<BlockPos> known = STATIONS.get(level.dimension());
        if (known == null || known.isEmpty()) {
            return List.of();
        }

        List<BlockPos> sorted = new ArrayList<>(new LinkedHashSet<>(known));
        sorted.sort((a, b) -> a.asLong() < b.asLong() ? -1 : a.asLong() > b.asLong() ? 1 : 0);

        List<RadioStationBlockEntity> found = new ArrayList<>();
        for (BlockPos pos : sorted) {
            if (!level.isLoaded(pos)) {
                continue;
            }
            if (level.getBlockEntity(pos) instanceof RadioStationBlockEntity station) {
                found.add(station);
            } else {
                known.remove(pos);
            }
        }
        return found;
    }
}
