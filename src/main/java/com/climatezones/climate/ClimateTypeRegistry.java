package com.climatezones.climate;

import com.climatezones.ClimateZonesMod;

public final class ClimateTypeRegistry {
    public static final String MOD_ID = ClimateZonesMod.MOD_ID;

    private ClimateTypeRegistry() {
    }

    public static ClimateType resolve(String id) {
        return ClimateType.fromId(id);
    }
}
