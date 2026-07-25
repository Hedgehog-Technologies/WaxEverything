package org.hedgetech.waxeverything;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.hedgetech.waxeverything.network.SyncWaxStatePacket;
import org.hedgetech.waxeverything.network.SyncWaxedChunkPacket;
import org.hedgetech.waxeverything.network.WaxNetworkHelper;
import org.hedgetech.waxeverything.waxtracking.WaxManager;

/**
 * Fabric Server Entry Point
 */
public class FabricWaxEverything implements ModInitializer {
    
    @Override
    public void onInitialize() {
        CommonClass.init();
        registerPackets();
        registerServerEvents();
    }

    private static void registerPackets() {
        PayloadTypeRegistry.clientboundPlay().register(SyncWaxedChunkPacket.TYPE, SyncWaxedChunkPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncWaxStatePacket.TYPE, SyncWaxStatePacket.STREAM_CODEC);
    }

    private static void registerServerEvents() {
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
            WaxManager.onChunkLoad(level, chunk.getPos());
            for (ServerPlayer player : level.players()) {
                WaxNetworkHelper.sendChunkSync(level, chunk.getPos(), player);
            }
        });

        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            WaxManager.onChunkUnload(level, chunk.getPos());
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            ServerLevel serverLevel = player.level();
            WaxNetworkHelper.sendAllLoadedChunksToPlayer(serverLevel, player);
        });
    }

    /**
     * Default Constructor
     */
    public FabricWaxEverything() {}

}
