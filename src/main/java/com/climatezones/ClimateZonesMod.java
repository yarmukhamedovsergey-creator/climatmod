package com.climatezones;

import com.climatezones.command.ClimateCommand;
import com.climatezones.config.ModConfig;
import com.climatezones.network.ClimateNetworking;
import com.climatezones.player.PlayerClimateManager;
import com.climatezones.server.ClimateServerTicker;
import com.climatezones.zone.ClimateZoneManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClimateZonesMod implements ModInitializer {
    public static final String MOD_ID = "climatezones";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModConfig.load();
        ClimateZoneManager.getInstance().load();
        ClimateNetworking.register();
        ClimateNetworking.registerServer();
        PlayerClimateManager.init();

        CommandRegistrationCallback.EVENT.register(ClimateCommand::register);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ClimateZoneManager.getInstance().broadcastZones(server);
        });

        ServerTickEvents.END_SERVER_TICK.register(ClimateServerTicker::tick);

        LOGGER.info("Climate Zones initialized");
    }
}
