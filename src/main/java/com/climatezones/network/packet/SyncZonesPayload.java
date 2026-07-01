package com.climatezones.network.packet;

import com.climatezones.ClimateZonesMod;
import com.climatezones.climate.ClimateType;
import com.climatezones.zone.ClimateZone;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record SyncZonesPayload(List<ZoneEntry> zones) implements CustomPayload {
    public static final CustomPayload.Id<SyncZonesPayload> ID =
            new CustomPayload.Id<>(Identifier.of(ClimateZonesMod.MOD_ID, "sync_zones"));

    public static final PacketCodec<RegistryByteBuf, SyncZonesPayload> CODEC = PacketCodec.tuple(
            ZoneEntry.CODEC.collect(PacketCodecs.toList()),
            SyncZonesPayload::zones,
            SyncZonesPayload::new
    );

    public static SyncZonesPayload fromZones(Collection<ClimateZone> zones) {
        List<ZoneEntry> entries = new ArrayList<>();
        for (ClimateZone zone : zones) {
            entries.add(ZoneEntry.fromZone(zone));
        }
        return new SyncZonesPayload(entries);
    }

    public record ZoneEntry(
            String name,
            String typeId,
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ
    ) {
        public static final PacketCodec<RegistryByteBuf, ZoneEntry> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, ZoneEntry::name,
                PacketCodecs.STRING, ZoneEntry::typeId,
                PacketCodecs.INTEGER, ZoneEntry::minX,
                PacketCodecs.INTEGER, ZoneEntry::minY,
                PacketCodecs.INTEGER, ZoneEntry::minZ,
                PacketCodecs.INTEGER, ZoneEntry::maxX,
                PacketCodecs.INTEGER, ZoneEntry::maxY,
                PacketCodecs.INTEGER, ZoneEntry::maxZ,
                ZoneEntry::new
        );

        public static ZoneEntry fromZone(ClimateZone zone) {
            return new ZoneEntry(
                    zone.getName(),
                    zone.getClimateType().getId(),
                    zone.getMinX(), zone.getMinY(), zone.getMinZ(),
                    zone.getMaxX(), zone.getMaxY(), zone.getMaxZ()
            );
        }

        public ClimateZone toZone() {
            ClimateZone zone = new ClimateZone();
            zone.setName(name);
            zone.setClimateType(ClimateType.fromId(typeId));
            zone.setBounds(
                    new net.minecraft.util.math.BlockPos(minX, minY, minZ),
                    new net.minecraft.util.math.BlockPos(maxX, maxY, maxZ)
            );
            return zone;
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
