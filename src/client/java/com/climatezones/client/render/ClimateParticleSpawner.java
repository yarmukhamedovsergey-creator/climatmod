package com.climatezones.client.render;

import com.climatezones.client.ClientClimateState;
import com.climatezones.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public final class ClimateParticleSpawner {
    private ClimateParticleSpawner() {
    }

    public static void tick(MinecraftClient client) {
        if (!ModConfig.get().particlesEnabled || client.player == null || client.world == null) {
            return;
        }

        ClientClimateState state = ClientClimateState.get();
        float intensity = state.getParticleIntensity();
        if (intensity < 0.05f) {
            return;
        }

        ClientWorld world = client.world;
        Random random = world.random;
        int density = ModConfig.get().particleDensity;
        int count = MathHelper.ceil(density * intensity);

        double px = client.player.getX();
        double py = client.player.getY() + client.player.getHeight() * 0.5;
        double pz = client.player.getZ();

        if (state.getColdInfluence() > state.getHotInfluence()) {
            spawnColdParticles(world, random, px, py, pz, count, intensity);
        } else if (state.getHotInfluence() > 0) {
            spawnHotParticles(world, random, px, py, pz, count, intensity);
        }
    }

    private static void spawnColdParticles(ClientWorld world, Random random, double px, double py, double pz, int count, float intensity) {
        for (int i = 0; i < count; i++) {
            double ox = px + (random.nextDouble() - 0.5) * 12;
            double oy = py + random.nextDouble() * 4;
            double oz = pz + (random.nextDouble() - 0.5) * 12;
            world.addParticle(ParticleTypes.SNOWFLAKE, ox, oy, oz, 0, -0.02, 0);
        }

        if (random.nextFloat() < intensity * 0.3f) {
            world.addParticle(ParticleTypes.CLOUD,
                    px, py + 0.2, pz,
                    (random.nextDouble() - 0.5) * 0.02, 0.01, (random.nextDouble() - 0.5) * 0.02);
        }

        if (random.nextFloat() < intensity * 0.15f) {
            world.addParticle(ParticleTypes.WHITE_SMOKE,
                    px + random.nextGaussian() * 0.1,
                    py + 1.5,
                    pz + random.nextGaussian() * 0.1,
                    0, 0.02, 0);
        }
    }

    private static void spawnHotParticles(ClientWorld world, Random random, double px, double py, double pz, int count, float intensity) {
        for (int i = 0; i < count; i++) {
            double ox = px + (random.nextDouble() - 0.5) * 10;
            double oy = py - 0.5 + random.nextDouble() * 2;
            double oz = pz + (random.nextDouble() - 0.5) * 10;
            world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, ox, oy, oz,
                    (random.nextDouble() - 0.5) * 0.01, 0.02, (random.nextDouble() - 0.5) * 0.01);
        }

        if (random.nextFloat() < intensity * 0.4f) {
            world.addParticle(ParticleTypes.LANDING_LAVA,
                    px + (random.nextDouble() - 0.5) * 8,
                    py,
                    pz + (random.nextDouble() - 0.5) * 8,
                    0, 0.01, 0);
        }
    }
}
