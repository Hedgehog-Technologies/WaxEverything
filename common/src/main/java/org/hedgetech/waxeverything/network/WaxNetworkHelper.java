package org.hedgetech.waxeverything.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.hedgetech.waxeverything.saveddata.WaxedSavedData;

public final class WaxNetworkHelper {

    public static void sendWaxUpdate(ServerLevel level, BlockPos pos, boolean waxed) {
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(
                new SyncWaxStatePacket(pos.asLong(), waxed)
        );
        for (ServerPlayer player : level.players()) {
            player.connection.send(packet);
        }
    }

    public static void sendChunkSync(ServerLevel level, ChunkPos chunkPos, ServerPlayer player) {
        long[] blocks = WaxedSavedData.get(level).getBlocksForChunk(chunkPos);
        if (blocks != null && blocks.length > 0) {
            player.connection.send(new ClientboundCustomPayloadPacket(
                    new SyncWaxedChunkPacket(chunkPos.pack(), blocks)
            ));
        }
    }

    public static void sendAllLoadedChunksToPlayer(ServerLevel level, ServerPlayer player) {
        WaxedSavedData.get(level).forEachLoadedWaxChunk((chunkPos, blocks) -> {
            player.connection.send(new ClientboundCustomPayloadPacket(
                    new SyncWaxedChunkPacket(chunkPos, blocks)
            ));
        });
    }

    private WaxNetworkHelper() {}
}
