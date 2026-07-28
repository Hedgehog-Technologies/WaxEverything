package org.hedgetech.waxeverything.waxtracking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.hedgetech.waxeverything.Constants;

import java.util.*;

@Deprecated(forRemoval = true)
public class WaxedSavedData extends SavedData {

    private static final String DATA_NAME = "waxed_targets_data";

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, DATA_NAME);

    private record ChunkEntry(long chunkPos, List<WaxTarget> targets) {
        public static final Codec<ChunkEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.LONG.fieldOf("chunk_pos").forGetter(ChunkEntry::chunkPos),
                        WaxTarget.CODEC.listOf().fieldOf("targets").forGetter(ChunkEntry::targets)
                ).apply(instance, ChunkEntry::new)
        );
    }
    public static final Codec<WaxedSavedData> CODEC = ChunkEntry.CODEC.listOf().xmap(
            WaxedSavedData::fromEntries,
            WaxedSavedData::toEntries
    );

    public static SavedDataType<WaxedSavedData> TYPE = new SavedDataType<>(
            ID,
            WaxedSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<Long, Set<WaxTarget>> persistedChunks = new HashMap<>();
    private final Map<Long, Set<WaxTarget>> loadedCache = new HashMap<>();

    public WaxedSavedData() { }

    public static WaxedSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    // --- Chunk Lifecycle ---
    public void onChunkLoad(ChunkPos pos) {
        var key = pos.pack();
        var targets = persistedChunks.get(key);
        if (targets != null && !targets.isEmpty()) {
            loadedCache.put(key, new HashSet<>(targets));
        }
    }

    public void onChunkUnload(ChunkPos pos) {
        var key = pos.pack();
        loadedCache.remove(key);
    }

    // --- Mutators ---
    public boolean wax(WaxTarget target) {
        var chunkKey = getChunkKeyForTarget(target);
        var added = persistedChunks.computeIfAbsent(chunkKey, k -> new HashSet<>()).add(target);

        if (loadedCache.containsKey(chunkKey)) {
            loadedCache.get(chunkKey).add(target);
        }

        if (added) setDirty();

        return added;
    }

    public boolean unwax(WaxTarget target) {
        var chunkKey = getChunkKeyForTarget(target);
        var persistedSet = persistedChunks.get(chunkKey);
        var removed = persistedSet != null && persistedSet.remove(target);

        if (persistedSet != null && persistedSet.isEmpty()) {
            persistedChunks.remove(chunkKey);
        }

        var loadedSet = loadedCache.get(chunkKey);

        if (loadedSet != null) {
            loadedSet.remove(target);
            if (loadedSet.isEmpty()) loadedCache.remove(chunkKey);
        }

        if (removed) setDirty();

        return removed;
    }

    public boolean isWaxed(WaxTarget target) {
        var chunkKey = getChunkKeyForTarget(target);
        var targets = loadedCache.get(chunkKey);
        return targets != null && targets.contains(target);
    }

    public Set<WaxTarget> getTargetsForChunk(long packedChunkPos) {
        var targets = loadedCache.get(packedChunkPos);
        return targets != null
                ? Collections.unmodifiableSet(targets)
                : Collections.emptySet();
    }

    public static long getChunkKeyForTarget(WaxTarget target) {
        if (target instanceof WaxTarget.BlockTarget(BlockPos pos)) {
            return ChunkPos.pack(pos);
        } else if (target instanceof WaxTarget.EntityTarget entityTarget) {
            if (entityTarget.lastKnownPos().isPresent()) {
                var pos = entityTarget.lastKnownPos().get();
                return ChunkPos.pack(pos);
            }
        }

        return 0L;
    }

    // -- Codec Serialization Converters ---
    private static WaxedSavedData fromEntries(List<ChunkEntry> entries) {
        var data = new WaxedSavedData();

        for (ChunkEntry entry : entries) {
            if (!entry.targets.isEmpty()) {
                data.persistedChunks.put(entry.chunkPos, new HashSet<>(entry.targets));
            }
        }

        return data;
    }

    private List<ChunkEntry> toEntries() {
        var entries = new ArrayList<ChunkEntry>();
        var snapshot = new HashMap<>(persistedChunks);

        for (Map.Entry<Long, Set<WaxTarget>> entry : loadedCache.entrySet()) {
            if (entry.getValue().isEmpty()) {
                snapshot.remove(entry.getKey());
            } else {
                snapshot.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<Long, Set<WaxTarget>> entry : snapshot.entrySet()) {
            entries.add(new ChunkEntry(entry.getKey(), new ArrayList<>(entry.getValue())));
        }

        return entries;
    }
}
