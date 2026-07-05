package org.hedgetech.waxeverything.platform;

import org.hedgetech.waxeverything.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

/**
 * NeoForge Platform Helper
 */
public class NeoForgePlatformHelper implements IPlatformHelper {
    /**
     * Default Constructor - nothing special
     */
    public NeoForgePlatformHelper() { }

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }
}