package com.climatezones.client.render;

import com.climatezones.client.ClientClimateState;
import com.climatezones.config.ModConfig;

public final class ClimateFogState {
    private ClimateFogState() {
    }

    public static float getFogRed() {
        if (!ModConfig.get().fogEnabled) return -1;
        ClientClimateState state = ClientClimateState.get();
        float intensity = state.getFogIntensity();
        if (intensity < 0.01f) return -1;

        if (state.getColdInfluence() > state.getHotInfluence()) {
            return lerp(0.75f, 0.85f, intensity);
        }
        if (state.getHotInfluence() > 0) {
            return lerp(0.9f, 1.0f, intensity);
        }
        return -1;
    }

    public static float getFogGreen() {
        if (!ModConfig.get().fogEnabled) return -1;
        ClientClimateState state = ClientClimateState.get();
        float intensity = state.getFogIntensity();
        if (intensity < 0.01f) return -1;

        if (state.getColdInfluence() > state.getHotInfluence()) {
            return lerp(0.82f, 0.9f, intensity);
        }
        if (state.getHotInfluence() > 0) {
            return lerp(0.65f, 0.75f, intensity);
        }
        return -1;
    }

    public static float getFogBlue() {
        if (!ModConfig.get().fogEnabled) return -1;
        ClientClimateState state = ClientClimateState.get();
        float intensity = state.getFogIntensity();
        if (intensity < 0.01f) return -1;

        if (state.getColdInfluence() > state.getHotInfluence()) {
            return lerp(0.95f, 1.0f, intensity);
        }
        if (state.getHotInfluence() > 0) {
            return lerp(0.45f, 0.55f, intensity);
        }
        return -1;
    }

    public static float getFogStartMultiplier() {
        ClientClimateState state = ClientClimateState.get();
        float intensity = state.getFogIntensity();
        if (intensity < 0.01f) return 1.0f;
        return 1.0f - intensity * 0.5f;
    }

    public static float getFogEndMultiplier() {
        ClientClimateState state = ClientClimateState.get();
        float intensity = state.getFogIntensity();
        if (intensity < 0.01f) return 1.0f;
        return 1.0f - intensity * 0.35f;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
