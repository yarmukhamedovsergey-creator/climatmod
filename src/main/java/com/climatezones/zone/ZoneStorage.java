package com.climatezones.zone;

import com.climatezones.ClimateZonesMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ZoneStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type ZONE_LIST_TYPE = new TypeToken<List<ZoneData>>() {}.getType();

    private ZoneStorage() {
    }

    private static Path getFilePath() {
        return FabricLoader.getInstance().getConfigDir().resolve("climatezones").resolve("zones.json");
    }

    public static Map<String, ClimateZone> load() {
        Path path = getFilePath();
        Map<String, ClimateZone> result = new LinkedHashMap<>();
        if (!Files.exists(path)) {
            return result;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            List<ZoneData> data = GSON.fromJson(reader, ZONE_LIST_TYPE);
            if (data != null) {
                for (ZoneData entry : data) {
                    ClimateZone zone = entry.toZone();
                    if (zone.getClimateType() != null) {
                        result.put(zone.getName().toLowerCase(), zone);
                    }
                }
            }
        } catch (IOException e) {
            ClimateZonesMod.LOGGER.error("Failed to load climate zones", e);
        }
        return result;
    }

    public static void save(Collection<ClimateZone> zones) {
        Path path = getFilePath();
        try {
            Files.createDirectories(path.getParent());
            List<ZoneData> data = zones.stream().map(ZoneData::fromZone).toList();
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            ClimateZonesMod.LOGGER.error("Failed to save climate zones", e);
        }
    }

    private static class ZoneData {
        String name;
        String type;
        int minX;
        int minY;
        int minZ;
        int maxX;
        int maxY;
        int maxZ;

        static ZoneData fromZone(ClimateZone zone) {
            ZoneData data = new ZoneData();
            data.name = zone.getName();
            data.type = zone.getClimateType().getId();
            data.minX = zone.getMinX();
            data.minY = zone.getMinY();
            data.minZ = zone.getMinZ();
            data.maxX = zone.getMaxX();
            data.maxY = zone.getMaxY();
            data.maxZ = zone.getMaxZ();
            return data;
        }

        ClimateZone toZone() {
            ClimateZone zone = new ClimateZone();
            zone.setName(name);
            zone.setClimateType(com.climatezones.climate.ClimateType.fromId(type));
            zone.setBounds(
                    new net.minecraft.util.math.BlockPos(minX, minY, minZ),
                    new net.minecraft.util.math.BlockPos(maxX, maxY, maxZ)
            );
            return zone;
        }
    }
}
