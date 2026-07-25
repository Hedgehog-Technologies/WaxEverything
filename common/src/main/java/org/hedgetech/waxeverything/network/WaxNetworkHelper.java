package org.hedgetech.waxeverything.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import org.hedgetech.waxeverything.waxtracking.WaxTarget;
import org.hedgetech.waxeverything.waxtracking.WaxedSavedData;

import java.util.ArrayList;
import java.util.Optional;

public final class WaxNetworkHelper {

    // --- Block Conveniences ---
    public static void sendWaxUpdate(ServerLevel level, BlockPos pos, boolean waxed) {
        sendWaxUpdate(level, new WaxTarget.BlockTarget(pos), waxed);
    }

    // --- Entity Conveniences ---
    public static void sendWaxUpdate(ServerLevel level, Entity entity, boolean waxed) {
        sendWaxUpdate(level, new WaxTarget.EntityTarget(entity.getUUID(), Optional.of(entity.blockPosition())), waxed);
    }

    // --- Core Sync Methods ---
    public static void sendWaxUpdate(ServerLevel level, WaxTarget target, boolean waxed) {
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(
                new SyncWaxStatePacket(target, waxed)
        );

        for (ServerPlayer player : level.players()) {
            player.connection.send(packet);
        }
    }

    public static void sendChunkSync(ServerLevel level, ChunkPos chunkPos, ServerPlayer player) {
        var targets = WaxedSavedData.get(level).getTargetsForChunk(chunkPos.pack());
        if (!targets.isEmpty()) {
            player.connection.send(new ClientboundCustomPayloadPacket(
                    new SyncWaxedChunkPacket(chunkPos.pack(), new ArrayList<>(targets))
            ));
        }
    }

    public static void sendAllLoadedChunksToPlayer(ServerLevel level, ServerPlayer player) {
        var savedData = WaxedSavedData.get(level);
        player.getChunkTrackingView().forEach(chunkPos -> {
            var targets = savedData.getTargetsForChunk(chunkPos.pack());
            if (!targets.isEmpty()) {
                player.connection.send(new ClientboundCustomPayloadPacket(
                        new SyncWaxedChunkPacket(chunkPos.pack(), new ArrayList<>(targets))
                ));
            }
        });
    }

    private WaxNetworkHelper() {}
}
