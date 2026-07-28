package org.hedgetech.waxeverything.waxtracking;

import com.google.common.collect.MapMaker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import org.hedgetech.waxeverything.Constants;

import java.io.IOException;
import java.util.*;

public class WaxChunkStorage implements AutoCloseable {

    private static final Map<ServerLevel, WaxChunkStorage> INSTANCES = new MapMaker().weakKeys().makeMap();

    private final RegionFileStorage storage;
    private final Map<Long, Set<WaxTarget>> activeChunkCache = new HashMap<>();

    public static WaxChunkStorage get(ServerLevel level) {
        return INSTANCES.computeIfAbsent(level, WaxChunkStorage::new);
    }

    public WaxChunkStorage(ServerLevel level) {
        var worldRootPath = level.getServer().getWorldPath(LevelResource.ROOT);
        var dimensionFolderPath = DimensionType.getStorageFolder(level.dimension(), worldRootPath);
        var waxFolderPath = dimensionFolderPath.resolve(Constants.MOD_ID);
        var info = new RegionStorageInfo(Constants.MOD_ID, level.dimension(), Constants.MOD_ID);

        this.storage = new RegionFileStorage(info, waxFolderPath, false);
    }

    public void loadChunkData(ChunkPos chunkPos) {
        var packed = chunkPos.pack();
        try {
            var tag = this.storage.read(chunkPos);
            if (tag != null && tag.contains("targets")) {
                WaxTarget.CODEC.listOf().parse(NbtOps.INSTANCE, tag.get("targets"))
                        .resultOrPartial(err -> Constants.LOG.error("Failed to parse wax targets: {}", err))
                        .ifPresent(list -> activeChunkCache.put(packed, new HashSet<>(list)));
            }
        } catch (IOException e) {
            Constants.LOG.error("Failed to read wax region storage for chunk {}", chunkPos, e);
        }
    }

    public void saveAndUnloadChunkData(ChunkPos chunkPos) {
        var packed = chunkPos.pack();
        var targets = activeChunkCache.remove(packed);

        try {
            if (targets != null && !targets.isEmpty()) {
                var rootTag = new CompoundTag();
                WaxTarget.CODEC.listOf().encodeStart(NbtOps.INSTANCE, new ArrayList<>(targets))
                        .resultOrPartial(err -> Constants.LOG.error("Failed to encode wax targets: {}", err))
                        .ifPresent(encoded -> {
                            rootTag.put("targets", encoded);
                            try {
                                this.storage.write(chunkPos, rootTag);
                            } catch (IOException e) {
                                Constants.LOG.error("Failed to write chunk wax region data for {}", chunkPos, e);
                            }
                        });
            } else {
                this.storage.write(chunkPos, null);
            }
        } catch (IOException e) {
            Constants.LOG.error("Error updating region storage for chunk {}", chunkPos, e);
        }
    }

    public Set<WaxTarget> getTargets(ChunkPos chunkPos) {
        return activeChunkCache.getOrDefault(chunkPos.pack(), Collections.emptySet());
    }

    public boolean wax(ChunkPos chunkPos, WaxTarget target) {
        return activeChunkCache.computeIfAbsent(chunkPos.pack(), k -> new HashSet<>()).add(target);
    }

    public boolean unwax(ChunkPos chunkPos, WaxTarget target) {
        var packed = chunkPos.pack();
        var set = activeChunkCache.get(packed);

        if (set != null) {
            var removed = set.remove(target);
            if (set.isEmpty()) activeChunkCache.remove(packed);
            return removed;
        }

        return false;
    }

    public void flushAll() {
        try {
            this.storage.flush();
        } catch (IOException e) {
            Constants.LOG.error("Failed to flush wax region storage", e);
        }
    }

    @Override
    public void close() throws IOException {
        this.storage.close();
    }
}
