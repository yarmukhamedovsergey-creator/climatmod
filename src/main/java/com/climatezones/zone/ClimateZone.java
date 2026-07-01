package com.climatezones.zone;

import com.climatezones.climate.ClimateType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class ClimateZone {
    private String name;
    private ClimateType climateType;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;

    public ClimateZone() {
    }

    public ClimateZone(String name, ClimateType climateType, BlockPos pos1, BlockPos pos2) {
        this.name = name;
        this.climateType = climateType;
        setBounds(pos1, pos2);
    }

    public void setBounds(BlockPos pos1, BlockPos pos2) {
        this.minX = Math.min(pos1.getX(), pos2.getX());
        this.minY = Math.min(pos1.getY(), pos2.getY());
        this.minZ = Math.min(pos1.getZ(), pos2.getZ());
        this.maxX = Math.max(pos1.getX(), pos2.getX());
        this.maxY = Math.max(pos1.getY(), pos2.getY());
        this.maxZ = Math.max(pos1.getZ(), pos2.getZ());
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= minX && pos.getX() <= maxX
                && pos.getY() >= minY && pos.getY() <= maxY
                && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    public boolean contains(double x, double y, double z) {
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public double distanceToBorder(double x, double y, double z) {
        if (!contains(x, y, z)) {
            double dx = Math.max(minX - x, Math.max(0, x - maxX));
            double dy = Math.max(minY - y, Math.max(0, y - maxY));
            double dz = Math.max(minZ - z, Math.max(0, z - maxZ));
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        double toMinX = x - minX;
        double toMaxX = maxX - x;
        double toMinY = y - minY;
        double toMaxY = maxY - y;
        double toMinZ = z - minZ;
        double toMaxZ = maxZ - z;
        return Math.min(Math.min(Math.min(toMinX, toMaxX), Math.min(toMinY, toMaxY)), Math.min(toMinZ, toMaxZ));
    }

    public float getInfluence(double x, double y, double z, float transitionDistance) {
        if (transitionDistance <= 0) {
            return contains(x, y, z) ? 1.0f : 0.0f;
        }
        double borderDistance = distanceToBorder(x, y, z);
        if (borderDistance >= transitionDistance) {
            return 0.0f;
        }
        return (float) (1.0 - borderDistance / transitionDistance);
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", name);
        nbt.putString("type", climateType.getId());
        nbt.putInt("minX", minX);
        nbt.putInt("minY", minY);
        nbt.putInt("minZ", minZ);
        nbt.putInt("maxX", maxX);
        nbt.putInt("maxY", maxY);
        nbt.putInt("maxZ", maxZ);
        return nbt;
    }

    public static ClimateZone fromNbt(NbtCompound nbt) {
        ClimateZone zone = new ClimateZone();
        zone.name = nbt.getString("name");
        zone.climateType = ClimateType.fromId(nbt.getString("type"));
        zone.minX = nbt.getInt("minX");
        zone.minY = nbt.getInt("minY");
        zone.minZ = nbt.getInt("minZ");
        zone.maxX = nbt.getInt("maxX");
        zone.maxY = nbt.getInt("maxY");
        zone.maxZ = nbt.getInt("maxZ");
        return zone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClimateType getClimateType() {
        return climateType;
    }

    public void setClimateType(ClimateType climateType) {
        this.climateType = climateType;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public int getVolume() {
        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }
}
