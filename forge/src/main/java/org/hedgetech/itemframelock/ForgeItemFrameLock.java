package org.hedgetech.itemframelock;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import org.hedgetech.itemframelock.commands.CommandRegistry;

import java.util.Objects;

/**
 * Forge Entry Point
 */
@Mod(Constants.MOD_ID)
public class ForgeItemFrameLock {
    /**
     * constructor - entry point for Forge Mod Loader
     */
    public ForgeItemFrameLock() {
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
//        Constants.LOG.info("Hello Forge world!");
        CommonClass.init();
    }
}