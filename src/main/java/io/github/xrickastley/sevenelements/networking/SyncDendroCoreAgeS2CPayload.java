package io.github.xrickastley.sevenelements.networking;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SyncDendroCoreAgeS2CPayload(int entityId, int age) implements CustomPayload {
	public static final CustomPayload.Id<SyncDendroCoreAgeS2CPayload> ID = new CustomPayload.Id<>(
		SevenElements.identifier("s2c/sync_dendro_core_age")
	);

	public static final PacketCodec<RegistryByteBuf, SyncDendroCoreAgeS2CPayload> CODEC = PacketCodec.tuple(
		PacketCodecs.INTEGER, SyncDendroCoreAgeS2CPayload::entityId,
		PacketCodecs.INTEGER, SyncDendroCoreAgeS2CPayload::age,
		SyncDendroCoreAgeS2CPayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
