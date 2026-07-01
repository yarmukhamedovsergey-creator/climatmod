package com.climatezones.client;

import com.climatezones.network.packet.SyncPlayerClimatePayload;
import com.climatezones.network.packet.SyncZonesPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public final class ClimateClientHandler {
    private static float shakeOffsetX;
    private static float shakeOffsetY;

    private ClimateClientHandler() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SyncZonesPayload.ID, (payload, context) -> {
            context.client().execute(() -> ClientClimateState.get().updateZones(payload));
        });

        ClientPlayNetworking.registerGlobalReceiver(SyncPlayerClimatePayload.ID, (payload, context) -> {
            context.client().execute(() -> ClientClimateState.get().updatePlayer(payload));
        });
    }

    public static void tick(MinecraftClient client) {
        ClientClimateState.get().tick(client);
        updateShake(client);
    }

    private static void updateShake(MinecraftClient client) {
        ClientClimateState state = ClientClimateState.get();
        if (state.isShaking() && client.player != null && client.world != null) {
            float intensity = 1.0f - MathHelper.clamp((state.getDisplayBodyTemperature() - 30.0f) / 5.0f, 0, 1);
            long time = client.world.getTime();
            shakeOffsetX = (float) (Math.sin(time * 0.8) * 0.02 * intensity);
            shakeOffsetY = (float) (Math.cos(time * 1.1) * 0.015 * intensity);
        } else {
            shakeOffsetX = TemperatureMathApproach(shakeOffsetX, 0, 0.01f);
            shakeOffsetY = TemperatureMathApproach(shakeOffsetY, 0, 0.01f);
        }
    }

    private static float TemperatureMathApproach(float current, float target, float delta) {
        if (current < target) return Math.min(current + delta, target);
        return Math.max(current - delta, target);
    }

    public static float getShakeOffsetX() {
        return shakeOffsetX;
    }

    public static float getShakeOffsetY() {
        return shakeOffsetY;
    }
}
