package com.easeon.ss.copperoxidizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_DIR = new File("config/easeon");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "easeon.ss.copperoxidizer.json");

    private boolean enabled = true;
    private int requiredOpLevel = 2;
    private boolean consumeWater = true;
    private boolean showParticles = true;
    private boolean playSound = true;

    public void load() {
        if (!CONFIG_FILE.exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                this.enabled = data.enabled;
                this.requiredOpLevel = data.requiredOpLevel;
                this.consumeWater = data.consumeWater;
                this.showParticles = data.showParticles;
                this.playSound = data.playSound;
            }
            Easeon.LOGGER.info("Config loaded: enabled = {}, required OP level = {}, consume water = {}",
                    enabled, requiredOpLevel, consumeWater);
        } catch (IOException e) {
            Easeon.LOGGER.error("Failed to load config", e);
        }
    }

    public void save() {
        try {
            if (!CONFIG_DIR.exists()) {
                CONFIG_DIR.mkdirs();
            }

            ConfigData data = new ConfigData();
            data.enabled = this.enabled;
            data.requiredOpLevel = this.requiredOpLevel;
            data.consumeWater = this.consumeWater;
            data.showParticles = this.showParticles;
            data.playSound = this.playSound;

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(data, writer);
            }

            Easeon.LOGGER.info("Config saved: enabled = {}, required OP level = {}, consume water = {}",
                    enabled, requiredOpLevel, consumeWater);
        } catch (IOException e) {
            Easeon.LOGGER.error("Failed to save config", e);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public int getRequiredOpLevel() {
        return requiredOpLevel;
    }

    public boolean shouldConsumeWater() {
        return consumeWater;
    }

    public boolean shouldShowParticles() {
        return showParticles;
    }

    public boolean shouldPlaySound() {
        return playSound;
    }

    private static class ConfigData {
        public boolean enabled = true;
        public int requiredOpLevel = 2;
        public boolean consumeWater = true;
        public boolean showParticles = true;
        public boolean playSound = true;
    }
}