package com.climatezones.network.packet;

import com.climatezones.ClimateZonesMod;
import com.climatezones.climate.ClimateType;
import com.climatezones.player.PlayerClimateData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncPlayerClimatePayload(
        float airTemperature,
        float bodyTemperature,
        String climateTypeId,
        String zoneName,
        float zoneInfluence,
        float coldInfluence,
        float hotInfluence,
        boolean shaking,
        boolean freezing,
        boolean overheating,
        String statusKey
) implements CustomPayload {
    public static final CustomPayload.Id<SyncPlayerClimatePayload> ID =
            new CustomPayload.Id<>(Identifier.of(ClimateZonesMod.MOD_ID, "sync_player"));

    public static final PacketCodec<RegistryByteBuf, SyncPlayerClimatePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, SyncPlayerClimatePayload::airTemperature,
            PacketCodecs.FLOAT, SyncPlayerClimatePayload::bodyTemperature,
            PacketCodecs.STRING, SyncPlayerClimatePayload::climateTypeId,
            PacketCodecs.STRING, SyncPlayerClimatePayload::zoneName,
            PacketCodecs.FLOAT, SyncPlayerClimatePayload::zoneInfluence,
            PacketCodecs.FLOAT, SyncPlayerClimatePayload::coldInfluence,
            PacketCodecs.FLOAT, SyncPlayerClimatePayload::hotInfluence,
            PacketCodecs.BOOLEAN, SyncPlayerClimatePayload::shaking,
            PacketCodecs.BOOLEAN, SyncPlayerClimatePayload::freezing,
            PacketCodecs.BOOLEAN, SyncPlayerClimatePayload::overheating,
            PacketCodecs.STRING, SyncPlayerClimatePayload::statusKey,
            SyncPlayerClimatePayload::new
    );

    public static SyncPlayerClimatePayload fromData(PlayerClimateData data) {
        ClimateType type = data.getCurrentClimateType();
        return new SyncPlayerClimatePayload(
                data.getAirTemperature(),
                data.getBodyTemperature(),
                type != null ? type.getId() : "",
                data.getZoneName(),
                data.getZoneInfluence(),
                data.getColdInfluence(),
                data.getHotInfluence(),
                data.isShaking(),
                data.isFreezing(),
                data.isOverheating(),
                data.getStatusKey()
        );
    }

    public ClimateType getClimateType() {
        return climateTypeId.isEmpty() ? null : ClimateType.fromId(climateTypeId);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
