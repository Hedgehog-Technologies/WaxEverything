package org.hedgetech.waxeverything.saveddata;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;

public final class ClientWaxRegistry {

    private static final HashMap<Long, LongSet> chunkData = new HashMap<>();

    public static void onChunkSync(long chunkPos, long[] blockPositions) {
        if (blockPositions.length == 0) {
            chunkData.remove(chunkPos);
            return;
        }

        LongSet set = new LongOpenHashSet(blockPositions.length * 2);
        for (long l : blockPositions) set.add(l);
        chunkData.put(chunkPos, set);
    }

    public static void onWaxStateUpdate(long packedBlockPos, boolean waxed) {
        BlockPos pos = BlockPos.of(packedBlockPos);
        long chunkKey = ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4);

        if (waxed) {
            chunkData.computeIfAbsent(chunkKey, k -> new LongOpenHashSet()).add(packedBlockPos);
        } else {
            LongSet set = chunkData.get(chunkKey);
            if (set != null) {
                set.remove(packedBlockPos);
                if (set.isEmpty()) chunkData.remove(chunkKey);
            }
        }
    }

    public static void onChunkUnload(ChunkPos pos) {
        chunkData.remove(pos.pack());
    }

    public static boolean isWaxed(BlockPos pos) {
        long chunkKey = ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4);
        LongSet set = chunkData.get(chunkKey);
        return set != null && set.contains(pos.asLong());
    }

    public static void clear() {
        chunkData.clear();
    }

    private ClientWaxRegistry() {}
}
