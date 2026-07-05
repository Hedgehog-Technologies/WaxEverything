package org.hedgetech.waxeverything;

import net.minecraftforge.fml.common.Mod;

/**
 * Forge Entry Point
 */
@Mod(Constants.MOD_ID)
public class ForgeWaxEverything {
    /**
     * constructor - entry point for Forge Mod Loader
     */
    public ForgeWaxEverything() {
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
//        Constants.LOG.info("Hello Forge world!");
        CommonClass.init();
    }
}