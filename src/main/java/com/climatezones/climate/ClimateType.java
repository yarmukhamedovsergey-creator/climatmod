package com.climatezones.climate;

import com.climatezones.config.ModConfig;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public enum ClimateType {
    COLD("cold", "Arctic", "\u2744", -35.0f),
    HOT("hot", "Desert", "\uD83D\uDD25", 54.0f);

    private final String id;
    private final String displayName;
    private final String icon;
    private final float defaultAirTemperature;

    ClimateType(String id, String displayName, String icon, float defaultAirTemperature) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.defaultAirTemperature = defaultAirTemperature;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public float getDefaultAirTemperature() {
        return defaultAirTemperature;
    }

    public float getAirTemperature() {
        return switch (this) {
            case COLD -> ModConfig.get().coldAirTemperature;
            case HOT -> ModConfig.get().hotAirTemperature;
        };
    }

    public Text getDisplayText() {
        return Text.literal(icon + " " + displayName);
    }

    public Identifier getAmbientSoundId() {
        return switch (this) {
            case COLD -> Identifier.of(ClimateTypeRegistry.MOD_ID, "ambient.cold");
            case HOT -> Identifier.of(ClimateTypeRegistry.MOD_ID, "ambient.hot");
        };
    }

    public static ClimateType fromId(String id) {
        for (ClimateType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
