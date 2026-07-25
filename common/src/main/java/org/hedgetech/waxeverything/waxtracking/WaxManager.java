package org.hedgetech.waxeverything.waxtracking;

import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.core.jmx.Server;
import org.hedgetech.waxeverything.network.WaxNetworkHelper;

import java.util.Optional;

public final class WaxManager {

    public static void onChunkLoad(ServerLevel level, ChunkPos pos) {
        WaxedSavedData.get(level).onChunkLoad(pos);
    }

    public static void onChunkUnload(ServerLevel level, ChunkPos pos) {
        WaxedSavedData.get(level).onChunkUnload(pos);
    }

    // --- Block Conveniences ---
    public static boolean isWaxed(Level level, BlockPos pos) {
        return isWaxed(level, new WaxTarget.BlockTarget(pos));
    }

    public static boolean wax(Level level, BlockPos pos) {
        return wax(level, new WaxTarget.BlockTarget(pos));
    }

    public static boolean unwax(Level level, BlockPos pos) {
        return unwax(level, new WaxTarget.BlockTarget(pos));
    }

    // --- Entity Conveniences ---
    public static boolean isWaxed(Level level, Entity entity) {
        return isWaxed(level, new WaxTarget.EntityTarget(entity.getUUID(), Optional.of(entity.blockPosition())));
    }

    public static boolean wax(Level level, Entity entity) {
        return wax(level, new WaxTarget.EntityTarget(entity.getUUID(), Optional.of(entity.blockPosition())));
    }

    public static boolean unwax(Level level, Entity entity) {
        return unwax(level, new WaxTarget.EntityTarget(entity.getUUID(), Optional.of(entity.blockPosition())));
    }

    // --- Core Logic ---
    public static boolean isWaxed(Level level, WaxTarget target) {
        if (level.isClientSide()) {
            return ClientWaxRegistry.isWaxed(target);
        }

        if (level instanceof ServerLevel serverLevel) {
            return WaxedSavedData.get(serverLevel).isWaxed(target);
        }

        return false;
    }

    public static boolean wax(Level level, WaxTarget target) {
        if (level instanceof ServerLevel serverLevel) {
            var success = WaxedSavedData.get(serverLevel).wax(target);

            if (success) {
                WaxNetworkHelper.sendWaxUpdate(serverLevel, target, true);
            }

            return success;
        }

        return false;
    }

    public static boolean unwax(Level level, WaxTarget target) {
        if (level instanceof ServerLevel serverLevel) {
            var success = WaxedSavedData.get(serverLevel).unwax(target);

            if (success) {
                WaxNetworkHelper.sendWaxUpdate(serverLevel, target, false);
            }

            return success;
        }

        return false;
    }

//    public static void onChunkLoad(ServerLevel level, ChunkPos pos) {
//        WaxedSavedData.get(level).onChunkLoad(pos);
//    }
//
//    public static void onChunkUnload(ServerLevel level, ChunkPos pos) {
//        WaxedSavedData.get(level).onChunkUnload(pos);
//    }
//
//    public static void wax(ServerLevel level, BlockPos pos) {
//        WaxedSavedData.get(level).wax(pos);
//        WaxNetworkHelper.sendWaxUpdate(level, pos, true);
//    }
//
//    public static void unwax(ServerLevel level, BlockPos pos) {
//        WaxedSavedData.get(level).unwax(pos);
//        WaxNetworkHelper.sendWaxUpdate(level, pos, false);
//    }
//
//    public static boolean isWaxed(ServerLevel level, BlockPos pos) {
//        return WaxedSavedData.get(level).isWaxed(pos);
//    }
//
//    private WaxManager() {}
}
