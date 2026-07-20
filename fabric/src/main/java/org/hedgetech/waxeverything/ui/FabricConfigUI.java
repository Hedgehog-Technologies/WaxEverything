package org.hedgetech.waxeverything.ui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.hedgetech.waxeverything.Constants;
import org.hedgetech.waxeverything.WaxEverything;
import org.hedgetech.waxeverything.config.WaxEverythingConfig;
import org.hedgetech.waxeverything.platform.Services;

import java.awt.*;

public class FabricConfigUI implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (Services.PLATFORM.isModLoaded("yet_another_config_lib_v3")) {
            return parentScreen -> createScreen(parentScreen, WaxEverythingConfig.CONFIG);
        }
        return parentScreen -> null;
    }

    private static Screen createScreen(Screen parent, WaxEverythingConfig config) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Wax Everything Config"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Client Settings"))
                        .tooltip(Component.literal("Client-side settings for Wax Everything mod"))
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Waxed Overlay Color"))
                                .description(OptionDescription.of(Component.literal("The color of the overlay when a block is waxed.")))
                                .binding(WaxEverythingConfig.DEFAULT_OVERLAY_COLOR, config::overlayColor, config::setOverlayColor)
                                .controller(ColorControllerBuilder::create)
                                .build()
                        )
                        .build()
                )
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Server Settings"))
                        .tooltip(Component.literal("Server-side settings for Wax Everything Mod"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Storage Locked When Waxed"))
                                .description(OptionDescription.of(Component.literal("If true, storage blocks (like chests) will be locked when waxed, preventing interaction. Excludes Copper Chest variants.")))
                                .binding(WaxEverythingConfig.DEFAULT_STORAGE_LOCKED_WHEN_WAXED, () -> config.storageLockedWhenWaxed, val -> config.storageLockedWhenWaxed = val)
                                .controller(BooleanControllerBuilder::create)
                                .build()
                        )
                        .build()
                )
                .save(() -> WaxEverythingConfig.save(config, WaxEverythingConfig.CONFIG_PATH))
                .build()
                .generateScreen(parent);
    }
}
