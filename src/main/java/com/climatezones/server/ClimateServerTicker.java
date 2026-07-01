package com.climatezones.server;

import com.climatezones.config.ModConfig;
import com.climatezones.player.PlayerClimateManager;
import com.climatezones.zone.ClimateZoneManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ClimateServerTicker {
    private ClimateServerTicker() {
    }

    public static void tick(MinecraftServer server) {
        if (server.getTicks() % 1 != 0) {
            return;
        }

        ClimateZoneManager manager = ClimateZoneManager.getInstance();
        ModConfig config = ModConfig.get();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (manager.getZonesNear(player.getX(), player.getY(), player.getZ(), config.syncRange).isEmpty()
                    && PlayerClimateManager.get(player).getZoneInfluence() <= 0.01f) {
                continue;
            }
            PlayerClimateManager.tickPlayer(player);
        }
    }
}
