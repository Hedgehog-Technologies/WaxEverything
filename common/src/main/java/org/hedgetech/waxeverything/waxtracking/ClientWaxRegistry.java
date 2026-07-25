package org.hedgetech.waxeverything.waxtracking;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.hedgetech.waxeverything.Constants;

import java.util.*;

public final class ClientWaxRegistry {

    private static final Map<Long, Set<WaxTarget>> waxedTargets = new HashMap<>();

    public static void onChunkSync(long chunkPos, List<WaxTarget> targets) {
        Constants.LOG.info("Chunk Sync: {}; Targets: {}", chunkPos, targets.size());

        if (targets.isEmpty()) {
            waxedTargets.remove(chunkPos);
        } else {
            waxedTargets.put(chunkPos, new HashSet<>(targets));
        }
    }

    public static void onWaxStateUpdate(WaxTarget target, boolean waxed) {
        var chunkKey = WaxedSavedData.getChunkKeyForTarget(target);

        if (waxed) {
            waxedTargets.computeIfAbsent(chunkKey, k -> new HashSet<>()).add(target);
        } else {
            var set = waxedTargets.get(chunkKey);
            if (set != null) {
                set.remove(target);
                if (set.isEmpty()) waxedTargets.remove(chunkKey);
            }
        }
    }

    public static void onChunkUnload(ChunkPos pos) {
        waxedTargets.remove(pos.pack());
    }

    public static boolean isWaxed(WaxTarget target) {
        var chunkKey = WaxedSavedData.getChunkKeyForTarget(target);
        var set = waxedTargets.get(chunkKey);
        return set != null && set.contains(target);
    }

    public static boolean isWaxed(BlockPos pos) {
        return isWaxed(new WaxTarget.BlockTarget(pos));
    }

    public static boolean isWaxed(Entity entity) {
        return isWaxed(new WaxTarget.EntityTarget(entity.getUUID(), Optional.of(entity.blockPosition())));
    }

    public static Set<WaxTarget> getAllWaxedTargets() {
        Set<WaxTarget> allTargets = new HashSet<>();
        for (Set<WaxTarget> set : waxedTargets.values()) {
            allTargets.addAll(set);
        }
        return Collections.unmodifiableSet(allTargets);
    }

    public static void clear() {
        waxedTargets.clear();
    }

    private ClientWaxRegistry() {}
}
