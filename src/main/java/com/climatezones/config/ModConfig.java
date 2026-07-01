package com.climatezones.config;

import com.climatezones.ClimateZonesMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig INSTANCE = new ModConfig();

    public float ambientAirTemperature = 20.0f;
    public float normalBodyTemperature = 36.6f;

    public float coldAirTemperature = -35.0f;
    public float hotAirTemperature = 54.0f;

    public float bodyTemperatureChangeSpeed = 0.02f;
    public float survivalModifierStrength = 0.15f;

    public float transitionDistance = 16.0f;
    public float syncRange = 32.0f;

    public float coldSlownessThreshold = 35.0f;
    public float coldFatigueThreshold = 34.0f;
    public float coldShakeThreshold = 33.0f;
    public float coldOverlayThreshold = 32.0f;
    public float hypothermiaThreshold = 30.0f;
    public float freezeDamage = 1.0f;
    public int freezeDamageInterval = 40;

    public float hotHungerThreshold = 38.0f;
    public float hotDistortionThreshold = 39.0f;
    public float heatstrokeThreshold = 42.0f;
    public float heatDamage = 1.0f;
    public int heatDamageInterval = 40;

    public float campfireWarmRadius = 4.0f;
    public float campfireWarmAmount = 8.0f;
    public float fireWarmRadius = 3.0f;
    public float fireWarmAmount = 6.0f;
    public float lavaWarmRadius = 5.0f;
    public float lavaWarmAmount = 15.0f;
    public float furnaceWarmRadius = 3.0f;
    public float furnaceWarmAmount = 5.0f;

    public float waterCoolRadius = 2.0f;
    public float waterCoolAmount = 10.0f;
    public float rainCoolAmount = 5.0f;
    public float shadeCoolAmount = 4.0f;
    public float shadeCheckHeight = 8.0f;

    public boolean hudEnabled = true;
    public int hudX = 10;
    public int hudY = 10;
    public float hudScale = 1.0f;
    public boolean hudShowStatus = true;

    public boolean particlesEnabled = true;
    public int particleDensity = 3;
    public boolean fogEnabled = true;
    public float fogIntensity = 1.0f;
    public boolean overlayEnabled = true;

    public float soundVolume = 0.6f;
    public float soundFadeSpeed = 0.05f;

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load() {
        Path path = getPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                }
            } catch (IOException e) {
                ClimateZonesMod.LOGGER.error("Failed to load config", e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        Path path = getPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            ClimateZonesMod.LOGGER.error("Failed to save config", e);
        }
    }

    public static void reload() {
        load();
    }

    private static Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("climatezones").resolve("config.json");
    }
}
