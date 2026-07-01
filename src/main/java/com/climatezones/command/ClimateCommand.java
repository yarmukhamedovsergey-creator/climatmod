package com.climatezones.command;

import com.climatezones.climate.ClimateType;
import com.climatezones.config.ModConfig;
import com.climatezones.zone.ClimateZone;
import com.climatezones.zone.ClimateZoneManager;
import com.climatezones.zone.ZoneSelection;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.Locale;

public final class ClimateCommand {
    private ClimateCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                  CommandRegistryAccess registryAccess,
                                  CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("climate")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("pos1").executes(ClimateCommand::setPos1))
                .then(CommandManager.literal("pos2").executes(ClimateCommand::setPos2))
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("cold");
                                    builder.suggest("hot");
                                    return builder.buildFuture();
                                })
                                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                        .executes(ClimateCommand::createZone))))
                .then(CommandManager.literal("save").executes(ClimateCommand::save))
                .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                .executes(ClimateCommand::deleteZone)))
                .then(CommandManager.literal("edit")
                        .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                .executes(ClimateCommand::editZone)))
                .then(CommandManager.literal("list").executes(ClimateCommand::listZones))
                .then(CommandManager.literal("info").executes(ClimateCommand::info))
                .then(CommandManager.literal("reload").executes(ClimateCommand::reload))
        );
    }

    private static ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return ctx.getSource().getPlayer();
    }

    private static int setPos1(CommandContext<ServerCommandSource> ctx) {
        try {
            ServerPlayerEntity player = getPlayer(ctx);
            BlockPos pos = player.getBlockPos();
            ClimateZoneManager.getInstance().getSelection().setPos1(player.getUuid(), pos);
            ctx.getSource().sendFeedback(() -> Text.literal("Position 1 set to ")
                    .append(formatPos(pos)), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }
    }

    private static int setPos2(CommandContext<ServerCommandSource> ctx) {
        try {
            ServerPlayerEntity player = getPlayer(ctx);
            BlockPos pos = player.getBlockPos();
            ClimateZoneManager.getInstance().getSelection().setPos2(player.getUuid(), pos);
            ctx.getSource().sendFeedback(() -> Text.literal("Position 2 set to ")
                    .append(formatPos(pos)), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }
    }

    private static int createZone(CommandContext<ServerCommandSource> ctx) {
        try {
            ServerPlayerEntity player = getPlayer(ctx);
            String typeId = StringArgumentType.getString(ctx, "type").toLowerCase(Locale.ROOT);
            String name = StringArgumentType.getString(ctx, "name");
            ClimateType type = ClimateType.fromId(typeId);

            if (type == null) {
                ctx.getSource().sendError(Text.literal("Unknown climate type. Use cold or hot."));
                return 0;
            }

            ZoneSelection selection = ClimateZoneManager.getInstance().getSelection();
            if (!selection.hasSelection(player.getUuid())) {
                ctx.getSource().sendError(Text.literal("Set pos1 and pos2 first."));
                return 0;
            }

            if (selection.isEditing(player.getUuid())) {
                String editingName = selection.getEditingZone(player.getUuid());
                if (ClimateZoneManager.getInstance().updateZoneBounds(editingName, selection.getPos1(player.getUuid()), selection.getPos2(player.getUuid()))) {
                    selection.clearEditing(player.getUuid());
                    ClimateZoneManager.getInstance().broadcastZones(ctx.getSource().getServer());
                    ctx.getSource().sendFeedback(() -> Text.literal("Updated zone bounds for ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal(editingName).formatted(Formatting.AQUA)), true);
                    return 1;
                }
            }

            if (ClimateZoneManager.getInstance().createZone(name, type, selection.getPos1(player.getUuid()), selection.getPos2(player.getUuid()))) {
                ClimateZoneManager.getInstance().broadcastZones(ctx.getSource().getServer());
                ctx.getSource().sendFeedback(() -> Text.literal("Created ")
                        .formatted(Formatting.GREEN)
                        .append(Text.literal(type.getDisplayName() + " zone '" + name + "'").formatted(Formatting.AQUA)), true);
                return 1;
            }

            ctx.getSource().sendError(Text.literal("A zone with that name already exists."));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }
    }

    private static int save(CommandContext<ServerCommandSource> ctx) {
        ClimateZoneManager.getInstance().save();
        ctx.getSource().sendFeedback(() -> Text.literal("Climate zones saved.").formatted(Formatting.GREEN), true);
        return 1;
    }

    private static int deleteZone(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (ClimateZoneManager.getInstance().deleteZone(name)) {
            ClimateZoneManager.getInstance().broadcastZones(ctx.getSource().getServer());
            ctx.getSource().sendFeedback(() -> Text.literal("Deleted zone ")
                    .formatted(Formatting.RED)
                    .append(Text.literal(name).formatted(Formatting.AQUA)), true);
            return 1;
        }
        ctx.getSource().sendError(Text.literal("Zone not found."));
        return 0;
    }

    private static int editZone(CommandContext<ServerCommandSource> ctx) {
        try {
            ServerPlayerEntity player = getPlayer(ctx);
            String name = StringArgumentType.getString(ctx, "name");
            if (ClimateZoneManager.getInstance().getZone(name).isEmpty()) {
                ctx.getSource().sendError(Text.literal("Zone not found."));
                return 0;
            }
            ClimateZoneManager.getInstance().getSelection().startEditing(player.getUuid(), name);
            ctx.getSource().sendFeedback(() -> Text.literal("Editing zone ")
                    .formatted(Formatting.YELLOW)
                    .append(Text.literal(name).formatted(Formatting.AQUA))
                    .append(Text.literal(". Set pos1 and pos2, then run /climate create <type> <name>.").formatted(Formatting.GRAY)), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }
    }

    private static int listZones(CommandContext<ServerCommandSource> ctx) {
        var zones = ClimateZoneManager.getInstance().getZones();
        if (zones.isEmpty()) {
            ctx.getSource().sendFeedback(() -> Text.literal("No climate zones defined.").formatted(Formatting.GRAY), false);
            return 1;
        }
        ctx.getSource().sendFeedback(() -> Text.literal("Climate Zones (" + zones.size() + "):").formatted(Formatting.GOLD), false);
        for (ClimateZone zone : zones) {
            ctx.getSource().sendFeedback(() -> Text.literal(" - ")
                    .append(Text.literal(zone.getName()).formatted(Formatting.AQUA))
                    .append(Text.literal(" [" + zone.getClimateType().getId() + "] ").formatted(Formatting.GRAY))
                    .append(formatBounds(zone)), false);
        }
        return zones.size();
    }

    private static int info(CommandContext<ServerCommandSource> ctx) {
        try {
            ServerPlayerEntity player = getPlayer(ctx);
            ZoneSelection selection = ClimateZoneManager.getInstance().getSelection();
            BlockPos p1 = selection.getPos1(player.getUuid());
            BlockPos p2 = selection.getPos2(player.getUuid());

            ctx.getSource().sendFeedback(() -> Text.literal("Climate Zone Info").formatted(Formatting.GOLD), false);
            ctx.getSource().sendFeedback(() -> Text.literal("Pos1: ").formatted(Formatting.GRAY)
                    .append(p1 != null ? formatPos(p1) : Text.literal("not set").formatted(Formatting.RED)), false);
            ctx.getSource().sendFeedback(() -> Text.literal("Pos2: ").formatted(Formatting.GRAY)
                    .append(p2 != null ? formatPos(p2) : Text.literal("not set").formatted(Formatting.RED)), false);

            if (selection.isEditing(player.getUuid())) {
                ctx.getSource().sendFeedback(() -> Text.literal("Editing: ")
                        .formatted(Formatting.YELLOW)
                        .append(Text.literal(selection.getEditingZone(player.getUuid())).formatted(Formatting.AQUA)), false);
            }

            var sample = ClimateZoneManager.getInstance().sampleAt(player.getX(), player.getY(), player.getZ());
            ctx.getSource().sendFeedback(() -> Text.literal("Current air temp: ")
                    .append(Text.literal(String.format("%.1f°C", sample.airTemperature())).formatted(Formatting.WHITE)), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }
    }

    private static int reload(CommandContext<ServerCommandSource> ctx) {
        ModConfig.reload();
        ClimateZoneManager.getInstance().reload();
        ClimateZoneManager.getInstance().broadcastZones(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal("Climate zones and config reloaded.").formatted(Formatting.GREEN), true);
        return 1;
    }

    private static Text formatPos(BlockPos pos) {
        return Text.literal(String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ())).formatted(Formatting.WHITE);
    }

    private static Text formatBounds(ClimateZone zone) {
        return Text.literal(String.format("(%d,%d,%d) -> (%d,%d,%d)",
                zone.getMinX(), zone.getMinY(), zone.getMinZ(),
                zone.getMaxX(), zone.getMaxY(), zone.getMaxZ())).formatted(Formatting.DARK_GRAY);
    }
}
