package com.climatezones.client.sound;

import com.climatezones.ClimateZonesMod;
import com.climatezones.client.ClientClimateState;
import com.climatezones.climate.ClimateType;
import com.climatezones.config.ModConfig;
import com.climatezones.util.TemperatureMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ClimateAmbientSoundManager {
    private static final Identifier COLD_AMBIENT = Identifier.of(ClimateZonesMod.MOD_ID, "ambient.cold");
    private static final Identifier HOT_AMBIENT = Identifier.of(ClimateZonesMod.MOD_ID, "ambient.hot");
    private static final Identifier WIND = Identifier.of(ClimateZonesMod.MOD_ID, "ambient.wind");

    private static ClimateType currentAmbient;
    private static float currentVolume;

    private ClimateAmbientSoundManager() {
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        ClientClimateState state = ClientClimateState.get();
        ClimateType targetType = null;
        float targetVolume = 0;

        if (state.inClimateZone()) {
            if (state.getColdInfluence() > state.getHotInfluence()) {
                targetType = ClimateType.COLD;
            } else if (state.getHotInfluence() > 0) {
                targetType = ClimateType.HOT;
            }
            targetVolume = state.getZoneInfluence() * ModConfig.get().soundVolume;
        }

        currentVolume = TemperatureMath.approach(currentVolume, targetVolume, ModConfig.get().soundFadeSpeed);

        if (targetType != currentAmbient && targetVolume > 0.05f) {
            playAmbient(client, targetType, currentVolume);
            currentAmbient = targetType;
        } else if (currentVolume < 0.02f) {
            currentAmbient = null;
        }

        if (state.getZoneInfluence() > 0.1f && client.world.random.nextInt(200) == 0) {
            playWind(client, currentVolume * 0.5f);
        }
    }

    private static void playAmbient(MinecraftClient client, ClimateType type, float volume) {
        Identifier id = type == ClimateType.COLD ? COLD_AMBIENT : HOT_AMBIENT;
        SoundEvent event = SoundEvent.of(id);
        client.getSoundManager().play(PositionedSoundInstance.master(event, 1.0f, volume, client.world.random));
    }

    private static void playWind(MinecraftClient client, float volume) {
        if (volume <= 0.01f) return;
        SoundEvent event = SoundEvent.of(WIND);
        client.getSoundManager().play(PositionedSoundInstance.master(event, 0.9f + client.world.random.nextFloat() * 0.2f,
                volume, client.world.random));
    }
}
