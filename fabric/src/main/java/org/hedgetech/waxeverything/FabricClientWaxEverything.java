package org.hedgetech.waxeverything;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.core.BlockPos;
import org.hedgetech.waxeverything.client.WaxOverlayRenderer;
import org.hedgetech.waxeverything.network.SyncWaxStatePacket;
import org.hedgetech.waxeverything.network.SyncWaxedChunkPacket;
import org.hedgetech.waxeverything.waxtracking.ClientWaxRegistry;

import java.util.List;

public class FabricClientWaxEverything implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        WaxOverlayRenderer.init();
        registerClientPacketHandlers();
        registerClientEvents();
        registerKeybinds();
    }

    private static void registerClientPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(SyncWaxedChunkPacket.TYPE,
                (payload, context) -> context.client().execute(() ->
                        ClientWaxRegistry.onChunkSync(payload.chunkPos(), payload.targets())));

        ClientPlayNetworking.registerGlobalReceiver(SyncWaxStatePacket.TYPE,
                (payload, context) -> context.client().execute(() ->
                        ClientWaxRegistry.onWaxStateUpdate(payload.target(), payload.waxed())));
    }

    private static void registerClientEvents() {
        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) ->
                ClientWaxRegistry.onChunkUnload(chunk.getPos())
        );

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                ClientWaxRegistry.clear()
        );

        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(context -> {
            if (!WaxOverlayRenderer.KEY_SHOW_WAXED.isDown()) return;

            var camera = context.levelState().cameraRenderState;
            var cameraPos = camera.pos;
            var poseStack = context.poseStack();
            var collector = context.submitNodeCollector();

            WaxOverlayRenderer.renderAll(poseStack, collector, cameraPos);
        });
    }

    private static void registerKeybinds() {
        KeyMappingHelper.registerKeyMapping(WaxOverlayRenderer.KEY_SHOW_WAXED);
    }

    public FabricClientWaxEverything() {}

}
