package org.hedgetech.waxeverything.ui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.hedgetech.waxeverything.config.WaxEverythingConfig;
import org.hedgetech.waxeverything.platform.Services;

import java.awt.*;

public class FabricConfigUI implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (Services.PLATFORM.isModLoaded("yet_another_config_lib_v3")) {
            return parentScreen -> createScreen(parentScreen, WaxEverythingConfig.CONFIG);
        }
        return _ -> null;
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
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Storage Interaction Options"))
                                .description(OptionDescription.of(Component.literal("Options related to how storage blocks behave when waxed.")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Wooden Chests Locked When Waxed"))
                                        .description(OptionDescription.of(Component.literal("If true, wood-based Chests will be locked when waxed, preventing interaction. Excludes Copper Chest variants.")))
                                        .binding(WaxEverythingConfig.DEFAULT_CHEST_LOCKED_WHEN_WAXED, () -> config.chestLockedWhenWaxed, val -> config.chestLockedWhenWaxed = val)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Copper Chests Locked When Waxed"))
                                        .description(OptionDescription.of(Component.literal("If true, Copper Chest variants will also be locked when waxed, preventing interaction.")))
                                        .binding(WaxEverythingConfig.DEFAULT_COPPER_CHEST_LOCKED_WHEN_WAXED, () -> config.copperChestLockedWhenWaxed, val -> config.copperChestLockedWhenWaxed = val)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Trapped Chests Locked When Waxed"))
                                        .description(OptionDescription.of(Component.literal("If true, Trapped Chests will be locked when waxed, preventing interaction.")))
                                        .binding(WaxEverythingConfig.DEFAULT_TRAPPED_CHEST_LOCKED_WHEN_WAXED, () -> config.trappedChestLockedWhenWaxed, val -> config.trappedChestLockedWhenWaxed = val)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Ender Chests Locked When Waxed"))
                                        .description(OptionDescription.of(Component.literal("If true, Ender Chests will be locked when waxed, preventing interaction.")))
                                        .binding(WaxEverythingConfig.DEFAULT_ENDER_CHEST_LOCKED_WHEN_WAXED, () -> config.enderChestLockedWhenWaxed, val -> config.enderChestLockedWhenWaxed = val)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Shulker Boxes Locked When Waxed"))
                                        .description(OptionDescription.of(Component.literal("If true, Shulker Boxes will be locked when waxed, preventing interaction.")))
                                        .binding(WaxEverythingConfig.DEFAULT_SHULKER_BOX_LOCKED_WHEN_WAXED, () -> config.shulkerBoxLockedWhenWaxed, val -> config.shulkerBoxLockedWhenWaxed = val)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Barrels Locked When Waxed"))
                                        .description(OptionDescription.of(Component.literal("If true, barrels will be locked when waxed, preventing interaction.")))
                                        .binding(WaxEverythingConfig.DEFAULT_BARREL_LOCKED_WHEN_WAXED, () -> config.barrelLockedWhenWaxed, val -> config.barrelLockedWhenWaxed = val)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Wax Prevents Chest Combine"))
                                        .description(OptionDescription.of(Component.literal("If true, waxed chests will not combine with its neighbors. Excludes Copper Chest variants.")))
                                        .binding(WaxEverythingConfig.DEFAULT_WAX_PREVENTS_CHEST_COMBINE, () -> config.waxPreventsChestCombine, val -> config.waxPreventsChestCombine = val)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Wax Prevents Copper Chest Combine"))
                                        .description(OptionDescription.of(Component.literal("If true, waxed copper chests will not combine with its neighbors.")))
                                        .binding(WaxEverythingConfig.DEFAULT_WAX_PREVENTS_COPPER_CHEST_COMBINE, () -> config.waxPreventsCopperChestCombine, val -> config.waxPreventsCopperChestCombine = val)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .save(() -> WaxEverythingConfig.save(config, WaxEverythingConfig.CONFIG_PATH))
                .build()
                .generateScreen(parent);
    }
}
