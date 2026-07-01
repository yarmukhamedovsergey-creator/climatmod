package com.climatezones.client;

import com.climatezones.climate.ClimateType;
import com.climatezones.config.ModConfig;
import com.climatezones.network.packet.SyncPlayerClimatePayload;
import com.climatezones.network.packet.SyncZonesPayload;
import com.climatezones.util.TemperatureMath;
import com.climatezones.zone.ClimateZone;
import net.minecraft.client.MinecraftClient;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientClimateState {
    private static final ClientClimateState INSTANCE = new ClientClimateState();

    private final Map<String, ClimateZone> zones = new LinkedHashMap<>();

    private float displayAirTemperature = 20.0f;
    private float displayBodyTemperature = 36.6f;
    private float targetAirTemperature = 20.0f;
    private float targetBodyTemperature = 36.6f;

    private ClimateType climateType;
    private String zoneName = "";
    private float zoneInfluence;
    private float coldInfluence;
    private float hotInfluence;
    private boolean shaking;
    private boolean freezing;
    private boolean overheating;
    private String statusKey = "status.climatezones.normal";

    private float fogIntensity;
    private float overlayIntensity;
    private float distortionIntensity;
    private float particleIntensity;

    public static ClientClimateState get() {
        return INSTANCE;
    }

    public void updateZones(SyncZonesPayload payload) {
        zones.clear();
        for (SyncZonesPayload.ZoneEntry entry : payload.zones()) {
            ClimateZone zone = entry.toZone();
            if (zone.getClimateType() != null) {
                zones.put(zone.getName().toLowerCase(), zone);
            }
        }
    }

    public void updatePlayer(SyncPlayerClimatePayload payload) {
        targetAirTemperature = payload.airTemperature();
        targetBodyTemperature = payload.bodyTemperature();
        climateType = payload.getClimateType();
        zoneName = payload.zoneName();
        zoneInfluence = payload.zoneInfluence();
        coldInfluence = payload.coldInfluence();
        hotInfluence = payload.hotInfluence();
        shaking = payload.shaking();
        freezing = payload.freezing();
        overheating = payload.overheating();
        statusKey = payload.statusKey();
    }

    public void tick(MinecraftClient client) {
        float speed = ModConfig.get().bodyTemperatureChangeSpeed;
        displayAirTemperature = TemperatureMath.approach(displayAirTemperature, targetAirTemperature, speed * 2);
        displayBodyTemperature = TemperatureMath.approach(displayBodyTemperature, targetBodyTemperature, speed);

        float targetFog = zoneInfluence * ModConfig.get().fogIntensity;
        float targetOverlay = freezing ? zoneInfluence : 0;
        float targetDistortion = overheating ? zoneInfluence : 0;
        float targetParticles = zoneInfluence * (ModConfig.get().particlesEnabled ? 1 : 0);

        float fade = ModConfig.get().soundFadeSpeed;
        fogIntensity = TemperatureMath.approach(fogIntensity, targetFog, fade);
        overlayIntensity = TemperatureMath.approach(overlayIntensity, targetOverlay, fade);
        distortionIntensity = TemperatureMath.approach(distortionIntensity, targetDistortion, fade);
        particleIntensity = TemperatureMath.approach(particleIntensity, targetParticles, fade);
    }

    public Collection<ClimateZone> getZones() {
        return zones.values();
    }

    public float getDisplayAirTemperature() {
        return displayAirTemperature;
    }

    public float getDisplayBodyTemperature() {
        return displayBodyTemperature;
    }

    public ClimateType getClimateType() {
        return climateType;
    }

    public String getZoneName() {
        return zoneName;
    }

    public float getZoneInfluence() {
        return zoneInfluence;
    }

    public float getColdInfluence() {
        return coldInfluence;
    }

    public float getHotInfluence() {
        return hotInfluence;
    }

    public boolean isShaking() {
        return shaking;
    }

    public boolean isFreezing() {
        return freezing;
    }

    public boolean isOverheating() {
        return overheating;
    }

    public String getStatusKey() {
        return statusKey;
    }

    public float getFogIntensity() {
        return fogIntensity;
    }

    public float getOverlayIntensity() {
        return overlayIntensity;
    }

    public float getDistortionIntensity() {
        return distortionIntensity;
    }

    public float getParticleIntensity() {
        return particleIntensity;
    }

    public boolean inClimateZone() {
        return zoneInfluence > 0.01f;
    }
}
