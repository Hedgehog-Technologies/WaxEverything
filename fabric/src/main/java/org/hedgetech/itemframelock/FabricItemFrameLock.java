package org.hedgetech.itemframelock;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.hedgetech.itemframelock.commands.CommandRegistry;

/**
 * Fabric Server Entry Point
 */
public class FabricItemFrameLock implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
//        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();

//        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
//                CommandRegistry.registerCommands(dispatcher)
//        );
//
//        ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler, minecraftServer) ->
//                ItemFrameLock.savePlayer(serverPlayNetworkHandler.getPlayer(), minecraftServer)
//        );
//
//        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) ->
//                ItemFrameLock.initPlayer(serverPlayNetworkHandler.getPlayer(), minecraftServer)
//        );
//
//        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
//                ItemFrameLock.onPlayerRespawn(oldPlayer.getStringUUID(), newPlayer)
//        );
    }

    /**
     * Default Constructor
     */
    public FabricItemFrameLock() { }
}
