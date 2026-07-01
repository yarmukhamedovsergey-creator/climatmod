package com.climatezones;

import com.climatezones.client.ClimateClientHandler;
import com.climatezones.client.hud.ClimateHudRenderer;
import com.climatezones.client.render.ClimateParticleSpawner;
import com.climatezones.client.sound.ClimateAmbientSoundManager;
import com.climatezones.network.ClimateNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class ClimateZonesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClimateNetworking.registerClient();
        ClimateClientHandler.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClimateAmbientSoundManager.tick(client);
            ClimateParticleSpawner.tick(client);
            ClimateClientHandler.tick(client);
        });

        HudRenderCallback.EVENT.register((context, tickDelta) -> ClimateHudRenderer.render(context, tickDelta));

        ClimateZonesMod.LOGGER.info("Climate Zones client initialized");
    }
}
