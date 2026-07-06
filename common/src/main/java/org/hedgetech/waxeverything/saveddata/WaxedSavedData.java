package org.hedgetech.waxeverything.saveddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.hedgetech.waxeverything.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class WaxedSavedData extends SavedData {
    private record ChunkEntry(long chunkPos, long[] blockPositions) {}

    private static final Codec<long[]> LONG_ARRAY_CODEC = Codec.LONG.listOf().xmap(
            list -> {
                long[] arr = new long[list.size()];
                for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
                return arr;
            },
            arr -> {
                List<Long> list = new ArrayList<>(arr.length);
                for (long v : arr) list.add(v);
                return list;
            }
    );

    private static final Codec<ChunkEntry> CHUNK_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("chunk").forGetter(ChunkEntry::chunkPos),
                    LONG_ARRAY_CODEC.fieldOf("blocks").forGetter(ChunkEntry::blockPositions)
            ).apply(instance, ChunkEntry::new)
    );

    public static final Codec<WaxedSavedData> CODEC = CHUNK_ENTRY_CODEC.listOf().xmap(
            WaxedSavedData::fromEntries,
            WaxedSavedData::toEntries
    );

    public static final SavedDataType<WaxedSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "waxed"),
            WaxedSavedData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final HashMap<Long, long[]> persistedChunks = new HashMap<>();

    private final HashMap<Long, LongSet> loadedCache = new HashMap<>();

    public static WaxedSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void onChunkLoad(ChunkPos pos) {
        long key = pos.pack();
        long[] raw = persistedChunks.get(key);
        if (raw != null && raw.length > 0) {
            LongSet set = new LongOpenHashSet(raw.length * 2);
            for (long l : raw) set.add(l);
            loadedCache.put(key, set);
        }
    }

    public void onChunkUnload(ChunkPos pos) {
        long key = pos.pack();
        LongSet set = loadedCache.remove(key);
        if (set == null) return;

        if (set.isEmpty()) {
            persistedChunks.remove(key);
        } else {
            persistedChunks.put(key, set.toLongArray());
        }
        setDirty();
    }

    public void wax(BlockPos pos) {
        long chunkKey = ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4);
        loadedCache.computeIfAbsent(chunkKey, k -> new LongOpenHashSet()).add(pos.asLong());
        setDirty();
    }

    public void unwax(BlockPos pos) {
        long chunkKey = ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4);
        LongSet set = loadedCache.get(chunkKey);
        if (set != null && set.remove(pos.asLong())) {
            setDirty();
        }
    }

    public boolean isWaxed(BlockPos pos) {
        long chunkKey = ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4);
        LongSet set = loadedCache.get(chunkKey);
        return set != null && set.contains(pos.asLong());
    }

    public long[] getBlocksForChunk(ChunkPos pos) {
        long key = pos.pack();
        LongSet loadedSet = loadedCache.get(key);
        if (loadedSet != null) return loadedSet.isEmpty() ? null : loadedSet.toLongArray();
        return persistedChunks.get(key);
    }

    public void forEachLoadedWaxChunk(BiConsumer<Long, long[]> consumer) {
        loadedCache.forEach((chunkKey, set) -> {
            if (!set.isEmpty()) consumer.accept(chunkKey, set.toLongArray());
        });
    }

    private static WaxedSavedData fromEntries(List<ChunkEntry> entries) {
        WaxedSavedData data = new WaxedSavedData();
        for (ChunkEntry e : entries) {
            if (e.blockPositions().length > 0) {
                data.persistedChunks.put(e.chunkPos(), e.blockPositions());
            }
        }
        return data;
    }

    private List<ChunkEntry> toEntries() {
        HashMap<Long, long[]> snapshot = new HashMap<>(persistedChunks);
        for (Map.Entry<Long, LongSet> entry : loadedCache.entrySet()) {
            if (entry.getValue().isEmpty()) {
                snapshot.remove(entry.getKey());
            } else {
                snapshot.put(entry.getKey(), entry.getValue().toLongArray());
            }
        }

        List<ChunkEntry> entries = new ArrayList<>(snapshot.size());
        snapshot.forEach((chunkKey, blocks) -> entries.add(new ChunkEntry(chunkKey, blocks)));
        return entries;
    }
}
