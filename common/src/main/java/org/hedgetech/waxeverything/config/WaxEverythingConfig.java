package org.hedgetech.waxeverything.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hedgetech.waxeverything.Constants;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WaxEverythingConfig {
    public static volatile WaxEverythingConfig CONFIG;

    public static final Path CONFIG_PATH = Paths.get("config").resolve("waxeverything.json");
    public static final String DEFAULT_OVERLAY_COLOR_HEX = "0xFFAE00";
    public static final Color DEFAULT_OVERLAY_COLOR = Color.decode(DEFAULT_OVERLAY_COLOR_HEX);
    public static final boolean DEFAULT_STORAGE_LOCKED_WHEN_WAXED = false;

    public int overlayColorInt = DEFAULT_OVERLAY_COLOR.getRGB();
    public float edgeWidth = 2.0F;
    public boolean storageLockedWhenWaxed = DEFAULT_STORAGE_LOCKED_WHEN_WAXED;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private static final List<Runnable> RELOAD_LISTENERS = new ArrayList<>();

    public Color overlayColor() {
        return Color.decode(String.valueOf(overlayColorInt));
    }

    public void setOverlayColor(Color color) {
        this.overlayColorInt = color.getRGB();
    }

    public static synchronized void init() {
        if (CONFIG == null) {
            CONFIG = load(CONFIG_PATH);
            Constants.LOG.trace("Config initialized: {}", CONFIG_PATH);
        } else {
            Constants.LOG.trace("Config is already initialized, skipping initialization.");
        }
    }

    private static WaxEverythingConfig createDefaultConfig() {
        Constants.LOG.trace("Creating default config file: {}", CONFIG_PATH);
        var defaultConfig = new WaxEverythingConfig();
        save(defaultConfig, CONFIG_PATH);
        return defaultConfig;
    }

    public static void registerReloadListener(Runnable listener) {
        RELOAD_LISTENERS.add(listener);
    }

    private static void notifyListeners() {
        for (var listener : RELOAD_LISTENERS) {
            try {
                listener.run();
            } catch (Exception e) {
                Constants.LOG.error("Error while notifying reload listener: {}", e.getMessage(), e);
            }
        }
    }

    public static WaxEverythingConfig load(Path path) {
        if (!Files.exists(path)) {
            Constants.LOG.trace("Config file does not exist, creating default config: {}", path);
            return createDefaultConfig();
        }

        try (var reader = Files.newBufferedReader(path)) {
            var config = GSON.fromJson(reader,  WaxEverythingConfig.class);

            if (config == null) {
                config = createDefaultConfig();
                Constants.LOG.trace("Config file was empty or invalid, created default config: {}", path);
            } else {
                Constants.LOG.trace("Config loaded from file: {}", path);
            }

            notifyListeners();
            return config;
        } catch (IOException e) {
            Constants.LOG.error("Failed to load config file: {} - {}", path, e);
            return createDefaultConfig();
        }
    }

    public static void save(WaxEverythingConfig config, Path path) {
        try {
            Constants.LOG.trace("Saving config file: {}", path);
            Files.createDirectories(path.getParent());
            try (var writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
            notifyListeners();
        } catch (IOException e) {
            Constants.LOG.error("Failed to save config file: {} - {}", path, e);
        }
    }

    private WaxEverythingConfig() { }
}
