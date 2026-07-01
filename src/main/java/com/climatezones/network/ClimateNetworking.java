package com.climatezones.network;

import com.climatezones.network.packet.SyncPlayerClimatePayload;
import com.climatezones.network.packet.SyncZonesPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ClimateNetworking {
    private ClimateNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(SyncZonesPayload.ID, SyncZonesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncPlayerClimatePayload.ID, SyncPlayerClimatePayload.CODEC);
    }

    public static void registerServer() {
        register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            server.execute(() -> com.climatezones.zone.ClimateZoneManager.getInstance().syncToPlayer(player));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                com.climatezones.player.PlayerClimateManager.remove(handler.getPlayer().getUuid()));
    }

    public static void registerClient() {
        // Payload types registered in common initializer
    }

    public static void sendZones(ServerPlayerEntity player, SyncZonesPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendPlayerClimate(ServerPlayerEntity player, SyncPlayerClimatePayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
