package com.climatezones.zone;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZoneSelection {
    private final Map<UUID, BlockPos> pos1 = new HashMap<>();
    private final Map<UUID, BlockPos> pos2 = new HashMap<>();
    private final Map<UUID, String> editingZone = new HashMap<>();

    public void setPos1(UUID playerId, BlockPos pos) {
        pos1.put(playerId, pos);
    }

    public void setPos2(UUID playerId, BlockPos pos) {
        pos2.put(playerId, pos);
    }

    public BlockPos getPos1(UUID playerId) {
        return pos1.get(playerId);
    }

    public BlockPos getPos2(UUID playerId) {
        return pos2.get(playerId);
    }

    public boolean hasSelection(UUID playerId) {
        return pos1.containsKey(playerId) && pos2.containsKey(playerId);
    }

    public void startEditing(UUID playerId, String zoneName) {
        editingZone.put(playerId, zoneName);
    }

    public String getEditingZone(UUID playerId) {
        return editingZone.get(playerId);
    }

    public boolean isEditing(UUID playerId) {
        return editingZone.containsKey(playerId);
    }

    public void clearEditing(UUID playerId) {
        editingZone.remove(playerId);
    }
}
