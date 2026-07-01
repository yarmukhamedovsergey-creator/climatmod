package com.climatezones.player;

import com.climatezones.config.ModConfig;
import com.climatezones.zone.ClimateZoneManager;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class SurvivalTemperatureHandler {
    private SurvivalTemperatureHandler() {
    }

    public static float calculateModifier(ServerPlayerEntity player) {
        ModConfig config = ModConfig.get();
        World world = player.getWorld();
        BlockPos playerPos = player.getBlockPos();
        float modifier = 0;

        modifier += scanWarmSources(world, playerPos, config);
        modifier += scanCoolSources(world, player, playerPos, config);

        return modifier * config.survivalModifierStrength;
    }

    private static float scanWarmSources(World world, BlockPos center, ModConfig config) {
        float warmth = 0;
        int radius = (int) Math.ceil(Math.max(config.lavaWarmRadius, config.campfireWarmRadius));
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 3; y++) {
                for (int z = -radius; z <= radius; z++) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    Block block = world.getBlockState(mutable).getBlock();
                    double dist = Math.sqrt(x * x + z * z);

                    if (block == Blocks.LAVA && dist <= config.lavaWarmRadius) {
                        warmth += config.lavaWarmAmount / (1 + dist);
                    } else if ((block == Blocks.FIRE || block == Blocks.SOUL_FIRE) && dist <= config.fireWarmRadius) {
                        warmth += config.fireWarmAmount / (1 + dist);
                    } else if (block instanceof CampfireBlock && dist <= config.campfireWarmRadius) {
                        warmth += config.campfireWarmAmount / (1 + dist);
                    } else if (isActiveFurnace(world, mutable) && dist <= config.furnaceWarmRadius) {
                        warmth += config.furnaceWarmAmount / (1 + dist);
                    }
                }
            }
        }
        return warmth;
    }

    private static boolean isActiveFurnace(World world, BlockPos pos) {
        return world.getBlockState(pos).contains(AbstractFurnaceBlock.LIT)
                && world.getBlockState(pos).get(AbstractFurnaceBlock.LIT);
    }

    private static float scanCoolSources(World world, ServerPlayerEntity player, BlockPos center, ModConfig config) {
        float cooling = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    Block block = world.getBlockState(mutable).getBlock();
                    double dist = Math.sqrt(x * x + y * y + z * z);
                    if ((block == Blocks.WATER || block == Blocks.BUBBLE_COLUMN) && dist <= config.waterCoolRadius) {
                        cooling -= config.waterCoolAmount / (1 + dist);
                    }
                }
            }
        }

        if (world.isRaining() && world.isSkyVisible(center)) {
            cooling -= config.rainCoolAmount;
        }

        if (isInShade(world, center, config.shadeCheckHeight)) {
            cooling -= config.shadeCoolAmount;
        }

        return cooling;
    }

    private static boolean isInShade(World world, BlockPos pos, float height) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int i = 1; i <= height; i++) {
            mutable.set(pos.getX(), pos.getY() + i, pos.getZ());
            if (!world.getBlockState(mutable).isAir() && world.getBlockState(mutable).isOpaque()) {
                return true;
            }
        }
        return false;
    }

    public static void applyEffects(ServerPlayerEntity player, PlayerClimateData data) {
        ModConfig config = ModConfig.get();
        ClimateZoneManager.ClimateSample sample = ClimateZoneManager.getInstance()
                .sampleAt(player.getX(), player.getY(), player.getZ());

        if (!sample.inClimateZone()) {
            clearClimateEffects(player);
            return;
        }

        if (sample.coldInfluence() > sample.hotInfluence()) {
            applyColdEffects(player, data, config);
        } else if (sample.hotInfluence() > 0) {
            applyHotEffects(player, data, config);
        } else {
            clearClimateEffects(player);
        }
    }

    private static void applyColdEffects(ServerPlayerEntity player, PlayerClimateData data, ModConfig config) {
        if (data.getBodyTemperature() <= config.coldFatigueThreshold) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 40, 0, false, false, true));
        }
        if (data.getBodyTemperature() <= config.coldSlownessThreshold) {
            int amplifier = data.getBodyTemperature() <= config.coldShakeThreshold ? 1 : 0;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, amplifier, false, false, true));
        }
    }

    private static void applyHotEffects(ServerPlayerEntity player, PlayerClimateData data, ModConfig config) {
        if (data.getBodyTemperature() >= config.hotHungerThreshold && player.getHungerManager().getFoodLevel() > 0) {
            if (player.age % 80 == 0) {
                player.getHungerManager().addExhaustion(0.25f * (data.getBodyTemperature() - config.normalBodyTemperature));
            }
        }
    }

    private static void clearClimateEffects(ServerPlayerEntity player) {
        player.removeStatusEffect(StatusEffects.SLOWNESS);
        player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
    }
}
