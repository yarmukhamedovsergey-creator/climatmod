package com.climatezones.client.hud;

import com.climatezones.climate.ClimateType;
import com.climatezones.client.ClientClimateState;
import com.climatezones.config.ModConfig;
import com.climatezones.util.TemperatureMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ClimateHudRenderer {
    private ClimateHudRenderer() {
    }

    public static void render(DrawContext context, float tickDelta) {
        ModConfig config = ModConfig.get();
        if (!config.hudEnabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        ClientClimateState state = ClientClimateState.get();
        if (!state.inClimateZone() && state.getZoneInfluence() < 0.01f) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        int x = (int) (config.hudX / config.hudScale);
        int y = (int) (config.hudY / config.hudScale);
        int lineHeight = 12;
        int padding = 6;
        int width = 130;
        int height = config.hudShowStatus ? 62 : 48;

        context.getMatrices().push();
        context.getMatrices().scale(config.hudScale, config.hudScale, 1.0f);

        int bgColor = 0xAA0D1117;
        int borderColor = getBorderColor(state);
        context.fill(x - padding, y - padding, x + width + padding, y + height + padding, bgColor);
        drawBorder(context, x - padding, y - padding, x + width + padding, y + height + padding, borderColor);

        ClimateType type = state.getClimateType();
        String climateLabel = type != null ? type.getIcon() + " " + type.getDisplayName() : state.getZoneName();
        context.drawText(textRenderer, Text.literal(climateLabel).formatted(Formatting.WHITE), x, y, 0xFFFFFF, true);
        y += lineHeight + 2;

        context.drawText(textRenderer, Text.literal("\uD83C\uDF21 Air").formatted(Formatting.AQUA), x, y, 0x66CCFF, false);
        String airTemp = TemperatureMath.formatTemperature(state.getDisplayAirTemperature());
        context.drawText(textRenderer, Text.literal(airTemp).formatted(Formatting.WHITE),
                x + width - textRenderer.getWidth(airTemp), y, 0xFFFFFF, true);
        y += lineHeight;

        context.drawText(textRenderer, Text.literal("\u2764 Body").formatted(Formatting.RED), x, y, 0xFF6666, false);
        String bodyTemp = TemperatureMath.formatTemperature(state.getDisplayBodyTemperature());
        int bodyColor = getBodyTemperatureColor(state.getDisplayBodyTemperature());
        context.drawText(textRenderer, Text.literal(bodyTemp),
                x + width - textRenderer.getWidth(bodyTemp), y, bodyColor, true);
        y += lineHeight;

        if (config.hudShowStatus) {
            Text status = Text.translatable(state.getStatusKey());
            Formatting statusColor = getStatusFormatting(state);
            context.drawText(textRenderer, Text.literal("Status: ").formatted(Formatting.GRAY)
                    .append(status.copy().formatted(statusColor)), x, y, 0xFFFFFF, true);
        }

        context.getMatrices().pop();
    }

    private static void drawBorder(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2, y1 + 1, color);
        context.fill(x1, y2 - 1, x2, y2, color);
        context.fill(x1, y1, x1 + 1, y2, color);
        context.fill(x2 - 1, y1, x2, y2, color);
    }

    private static int getBorderColor(ClientClimateState state) {
        if (state.isFreezing()) return 0xFF4FC3F7;
        if (state.isOverheating()) return 0xFFFF7043;
        if (state.getColdInfluence() > state.getHotInfluence()) return 0xFF81D4FA;
        if (state.getHotInfluence() > 0) return 0xFFFFAB40;
        return 0xFF546E7A;
    }

    private static int getBodyTemperatureColor(float bodyTemp) {
        ModConfig config = ModConfig.get();
        if (bodyTemp <= config.hypothermiaThreshold) return 0xFF4FC3F7;
        if (bodyTemp <= config.coldOverlayThreshold) return 0xFF81D4FA;
        if (bodyTemp >= config.heatstrokeThreshold) return 0xFFFF5252;
        if (bodyTemp >= config.hotDistortionThreshold) return 0xFFFF8A65;
        return 0xFFFFFFFF;
    }

    private static Formatting getStatusFormatting(ClientClimateState state) {
        if (state.isFreezing()) return Formatting.AQUA;
        if (state.isOverheating()) return Formatting.RED;
        if (state.getColdInfluence() > state.getHotInfluence()) return Formatting.BLUE;
        if (state.getHotInfluence() > 0) return Formatting.GOLD;
        return Formatting.GREEN;
    }
}
