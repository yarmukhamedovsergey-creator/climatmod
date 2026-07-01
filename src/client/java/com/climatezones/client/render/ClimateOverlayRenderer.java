package com.climatezones.client.render;

import com.climatezones.client.ClientClimateState;
import com.climatezones.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

public final class ClimateOverlayRenderer {
    private ClimateOverlayRenderer() {
    }

    public static void render(DrawContext context, int width, int height) {
        if (!ModConfig.get().overlayEnabled) {
            return;
        }

        ClientClimateState state = ClientClimateState.get();
        float overlay = state.getOverlayIntensity();
        float distortion = state.getDistortionIntensity();

        if (overlay > 0.01f) {
            int alpha = (int) (overlay * 120);
            int frostColor = (alpha << 24) | 0xB3E5FC;
            int edgeSize = (int) (40 + overlay * 60);

            context.fill(0, 0, width, edgeSize, frostColor);
            context.fill(0, height - edgeSize, width, height, frostColor);
            context.fill(0, 0, edgeSize, height, frostColor);
            context.fill(width - edgeSize, 0, width, height, frostColor);
        }

        if (distortion > 0.01f) {
            int alpha = (int) (distortion * 60);
            int heatColor = (alpha << 24) | 0xFF6D00;
            int wave = (int) (Math.sin(System.currentTimeMillis() * 0.005) * 4 * distortion);
            context.fill(0, wave, width, 8 + wave, heatColor);
            context.fill(0, height - 8 - wave, width, height - wave, heatColor);
            int sideAlpha = (int) (distortion * 30);
            int sideColor = (sideAlpha << 24) | 0xFFAB00;
            context.fill(0, 0, 6, height, sideColor);
            context.fill(width - 6, 0, width, height, sideColor);
        }
    }
}
