package com.climatezones.player;

import com.climatezones.config.ModConfig;
import com.climatezones.network.ClimateNetworking;
import com.climatezones.network.packet.SyncPlayerClimatePayload;
import com.climatezones.zone.ClimateZoneManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerClimateManager {
    private static final Map<UUID, PlayerClimateData> DATA = new HashMap<>();

    public static final RegistryKey<net.minecraft.entity.damage.DamageType> HYPOTHERMIA =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of("climatezones", "hypothermia"));
    public static final RegistryKey<net.minecraft.entity.damage.DamageType> HEATSTROKE =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of("climatezones", "heatstroke"));

    public static void init() {
    }

    public static PlayerClimateData get(ServerPlayerEntity player) {
        return DATA.computeIfAbsent(player.getUuid(), id -> new PlayerClimateData());
    }

    public static void remove(UUID playerId) {
        DATA.remove(playerId);
    }

    public static void tickPlayer(ServerPlayerEntity player) {
        PlayerClimateData data = get(player);
        ClimateZoneManager manager = ClimateZoneManager.getInstance();
        ModConfig config = ModConfig.get();

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        if (manager.getZonesNear(x, y, z, config.syncRange).isEmpty()) {
            data.setTargetAirTemperature(config.ambientAirTemperature);
            data.setCurrentClimateType(null);
            data.setZoneName("");
            data.setZoneInfluence(0);
            data.setColdInfluence(0);
            data.setHotInfluence(0);
            data.tickTowardTargets();
            syncIfNeeded(player, data);
            return;
        }

        ClimateZoneManager.ClimateSample sample = manager.sampleAt(x, y, z);
        data.setTargetAirTemperature(sample.airTemperature());
        data.setCurrentClimateType(sample.climateType());
        data.setZoneName(sample.zoneName() != null ? sample.zoneName() : "");
        data.setZoneInfluence(sample.influence());
        data.setColdInfluence(sample.coldInfluence());
        data.setHotInfluence(sample.hotInfluence());
        data.setSurvivalModifier(SurvivalTemperatureHandler.calculateModifier(player));

        data.tickTowardTargets();
        SurvivalTemperatureHandler.applyEffects(player, data);
        applyDamage(player, data);
        syncIfNeeded(player, data);
    }

    private static void applyDamage(ServerPlayerEntity player, PlayerClimateData data) {
        ModConfig config = ModConfig.get();

        if (data.getBodyTemperature() <= config.hypothermiaThreshold) {
            if (data.getFreezeDamageCooldown() <= 0) {
                DamageSource source = player.getDamageSources().create(HYPOTHERMIA);
                player.damage(source, config.freezeDamage);
                data.setFreezeDamageCooldown(config.freezeDamageInterval);
            } else {
                data.setFreezeDamageCooldown(data.getFreezeDamageCooldown() - 1);
            }
        } else {
            data.setFreezeDamageCooldown(0);
        }

        if (data.getBodyTemperature() >= config.heatstrokeThreshold) {
            if (data.getHeatDamageCooldown() <= 0) {
                DamageSource source = player.getDamageSources().create(HEATSTROKE);
                player.damage(source, config.heatDamage);
                data.setHeatDamageCooldown(config.heatDamageInterval);
            } else {
                data.setHeatDamageCooldown(data.getHeatDamageCooldown() - 1);
            }
        } else {
            data.setHeatDamageCooldown(0);
        }
    }

    private static void syncIfNeeded(ServerPlayerEntity player, PlayerClimateData data) {
        if (player.age % 5 == 0) {
            ClimateNetworking.sendPlayerClimate(player, SyncPlayerClimatePayload.fromData(data));
        }
    }
}
