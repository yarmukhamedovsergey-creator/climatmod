package com.climatezones.player;

import com.climatezones.climate.ClimateType;
import com.climatezones.config.ModConfig;
import com.climatezones.util.TemperatureMath;

public class PlayerClimateData {
    private float airTemperature = ModConfig.get().ambientAirTemperature;
    private float bodyTemperature = ModConfig.get().normalBodyTemperature;
    private float targetAirTemperature = airTemperature;
    private ClimateType currentClimateType;
    private String zoneName = "";
    private float zoneInfluence;
    private float coldInfluence;
    private float hotInfluence;
    private float survivalModifier;
    private int freezeDamageCooldown;
    private int heatDamageCooldown;
    private boolean shaking;
    private boolean overheating;
    private boolean freezing;

    public void tickTowardTargets() {
        float speed = ModConfig.get().bodyTemperatureChangeSpeed;
        airTemperature = TemperatureMath.approach(airTemperature, targetAirTemperature, speed * 2.0f);

        float equilibrium = targetAirTemperature + survivalModifier;
        bodyTemperature = TemperatureMath.approach(bodyTemperature, equilibrium, speed);

        updateStatusFlags();
    }

    private void updateStatusFlags() {
        ModConfig config = ModConfig.get();
        freezing = bodyTemperature <= config.coldOverlayThreshold;
        shaking = bodyTemperature <= config.coldShakeThreshold;
        overheating = bodyTemperature >= config.hotDistortionThreshold;
    }

    public float getAirTemperature() {
        return airTemperature;
    }

    public float getBodyTemperature() {
        return bodyTemperature;
    }

    public float getTargetAirTemperature() {
        return targetAirTemperature;
    }

    public void setTargetAirTemperature(float targetAirTemperature) {
        this.targetAirTemperature = targetAirTemperature;
    }

    public ClimateType getCurrentClimateType() {
        return currentClimateType;
    }

    public void setCurrentClimateType(ClimateType currentClimateType) {
        this.currentClimateType = currentClimateType;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName != null ? zoneName : "";
    }

    public float getZoneInfluence() {
        return zoneInfluence;
    }

    public void setZoneInfluence(float zoneInfluence) {
        this.zoneInfluence = zoneInfluence;
    }

    public float getColdInfluence() {
        return coldInfluence;
    }

    public void setColdInfluence(float coldInfluence) {
        this.coldInfluence = coldInfluence;
    }

    public float getHotInfluence() {
        return hotInfluence;
    }

    public void setHotInfluence(float hotInfluence) {
        this.hotInfluence = hotInfluence;
    }

    public float getSurvivalModifier() {
        return survivalModifier;
    }

    public void setSurvivalModifier(float survivalModifier) {
        this.survivalModifier = survivalModifier;
    }

    public int getFreezeDamageCooldown() {
        return freezeDamageCooldown;
    }

    public void setFreezeDamageCooldown(int freezeDamageCooldown) {
        this.freezeDamageCooldown = freezeDamageCooldown;
    }

    public int getHeatDamageCooldown() {
        return heatDamageCooldown;
    }

    public void setHeatDamageCooldown(int heatDamageCooldown) {
        this.heatDamageCooldown = heatDamageCooldown;
    }

    public boolean isShaking() {
        return shaking;
    }

    public boolean isOverheating() {
        return overheating;
    }

    public boolean isFreezing() {
        return freezing;
    }

    public String getStatusKey() {
        ModConfig config = ModConfig.get();
        if (bodyTemperature <= config.hypothermiaThreshold) {
            return "status.climatezones.freezing";
        }
        if (bodyTemperature <= config.coldOverlayThreshold) {
            return "status.climatezones.cold";
        }
        if (bodyTemperature >= config.heatstrokeThreshold) {
            return "status.climatezones.heatstroke";
        }
        if (bodyTemperature >= config.hotDistortionThreshold) {
            return "status.climatezones.overheating";
        }
        if (bodyTemperature >= config.hotHungerThreshold) {
            return "status.climatezones.warm";
        }
        return "status.climatezones.normal";
    }
}
