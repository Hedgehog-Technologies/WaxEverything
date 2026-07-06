package org.hedgetech.waxeverything;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.hedgetech.waxeverything.network.SyncWaxStatePacket;
import org.hedgetech.waxeverything.network.SyncWaxedChunkPacket;
import org.hedgetech.waxeverything.saveddata.ClientWaxRegistry;

public class FabricClientWaxEverything implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerClientPacketHandlers();
        registerClientEvents();
    }

    private static void registerClientPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(SyncWaxedChunkPacket.TYPE,
                (payload, context) -> context.client().execute(() ->
                        ClientWaxRegistry.onChunkSync(payload.chunkPos(), payload.blockPositions())));

        ClientPlayNetworking.registerGlobalReceiver(SyncWaxStatePacket.TYPE,
                (payload, context) -> context.client().execute(() ->
                        ClientWaxRegistry.onWaxStateUpdate(payload.blockPos(), payload.waxed())));
    }

    private static void registerClientEvents() {
        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) ->
                ClientWaxRegistry.onChunkUnload(chunk.getPos()));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                ClientWaxRegistry.clear());
    }

    public FabricClientWaxEverything() {}

}
