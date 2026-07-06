package org.hedgetech.waxeverything.saveddata;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.hedgetech.waxeverything.network.WaxNetworkHelper;

public final class WaxManager {
    public static void onChunkLoad(ServerLevel level, ChunkPos pos) {
        WaxedSavedData.get(level).onChunkLoad(pos);
    }

    public static void onChunkUnload(ServerLevel level, ChunkPos pos) {
        WaxedSavedData.get(level).onChunkUnload(pos);
    }

    public static void wax(ServerLevel level, BlockPos pos) {
        WaxedSavedData.get(level).wax(pos);
        WaxNetworkHelper.sendWaxUpdate(level, pos, true);
    }

    public static void unwax(ServerLevel level, BlockPos pos) {
        WaxedSavedData.get(level).unwax(pos);
        WaxNetworkHelper.sendWaxUpdate(level, pos, false);
    }

    public static boolean isWaxed(ServerLevel level, BlockPos pos) {
        return WaxedSavedData.get(level).isWaxed(pos);
    }

    private WaxManager() {}
}
