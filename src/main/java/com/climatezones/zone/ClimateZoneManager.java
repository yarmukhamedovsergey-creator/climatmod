package com.climatezones.zone;

import com.climatezones.ClimateZonesMod;
import com.climatezones.climate.ClimateType;
import com.climatezones.config.ModConfig;
import com.climatezones.network.ClimateNetworking;
import com.climatezones.network.packet.SyncZonesPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClimateZoneManager {
    private static final ClimateZoneManager INSTANCE = new ClimateZoneManager();

    private final Map<String, ClimateZone> zones = new LinkedHashMap<>();
    private final ZoneSelection selection = new ZoneSelection();

    public static ClimateZoneManager getInstance() {
        return INSTANCE;
    }

    public ZoneSelection getSelection() {
        return selection;
    }

    public Collection<ClimateZone> getZones() {
        return zones.values();
    }

    public Optional<ClimateZone> getZone(String name) {
        return Optional.ofNullable(zones.get(name.toLowerCase()));
    }

    public boolean createZone(String name, ClimateType type, BlockPos pos1, BlockPos pos2) {
        String key = name.toLowerCase();
        if (zones.containsKey(key)) {
            return false;
        }
        ClimateZone zone = new ClimateZone(name, type, pos1, pos2);
        zones.put(key, zone);
        save();
        return true;
    }

    public boolean deleteZone(String name) {
        ClimateZone removed = zones.remove(name.toLowerCase());
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    public boolean updateZoneBounds(String name, BlockPos pos1, BlockPos pos2) {
        ClimateZone zone = zones.get(name.toLowerCase());
        if (zone == null) {
            return false;
        }
        zone.setBounds(pos1, pos2);
        save();
        return true;
    }

    public ClimateSample sampleAt(double x, double y, double z) {
        float transitionDistance = ModConfig.get().transitionDistance;
        float ambientTemp = ModConfig.get().ambientAirTemperature;
        ClimateType dominantType = null;
        float dominantInfluence = 0;
        float totalColdInfluence = 0;
        float totalHotInfluence = 0;
        float weightedAirTemp = 0;
        float totalWeight = 0;
        String zoneName = null;

        for (ClimateZone zone : zones.values()) {
            float influence = zone.getInfluence(x, y, z, transitionDistance);
            if (influence <= 0) {
                continue;
            }
            float airTemp = zone.getClimateType().getAirTemperature();
            weightedAirTemp += airTemp * influence;
            totalWeight += influence;

            if (zone.getClimateType() == ClimateType.COLD) {
                totalColdInfluence += influence;
            } else if (zone.getClimateType() == ClimateType.HOT) {
                totalHotInfluence += influence;
            }

            if (influence > dominantInfluence) {
                dominantInfluence = influence;
                dominantType = zone.getClimateType();
                zoneName = zone.getName();
            }
        }

        float airTemperature;
        if (totalWeight > 0) {
            airTemperature = weightedAirTemp / totalWeight;
        } else {
            airTemperature = ambientTemp;
        }

        return new ClimateSample(airTemperature, dominantType, zoneName, dominantInfluence,
                totalColdInfluence, totalHotInfluence);
    }

    public List<ClimateZone> getZonesNear(double x, double y, double z, double range) {
        List<ClimateZone> result = new ArrayList<>();
        for (ClimateZone zone : zones.values()) {
            if (zone.contains(x, y, z) || zone.distanceToBorder(x, y, z) <= range) {
                result.add(zone);
            }
        }
        return result;
    }

    public void load() {
        zones.clear();
        zones.putAll(ZoneStorage.load());
        ClimateZonesMod.LOGGER.info("Loaded {} climate zones", zones.size());
    }

    public void save() {
        ZoneStorage.save(zones.values());
    }

    public void reload() {
        load();
    }

    public void broadcastZones(MinecraftServer server) {
        SyncZonesPayload payload = SyncZonesPayload.fromZones(zones.values());
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ClimateNetworking.sendZones(player, payload);
        }
    }

    public void syncToPlayer(ServerPlayerEntity player) {
        ClimateNetworking.sendZones(player, SyncZonesPayload.fromZones(zones.values()));
    }

    public record ClimateSample(
            float airTemperature,
            ClimateType climateType,
            String zoneName,
            float influence,
            float coldInfluence,
            float hotInfluence
    ) {
        public boolean inClimateZone() {
            return influence > 0.01f;
        }
    }
}
