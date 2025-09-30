package io.github.xrickastley.sevenelements.networking;

import java.util.List;
import java.util.stream.Collectors;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ShowElectroChargeS2CPayload(int mainEntity, List<Integer> otherEntities) implements CustomPayload {
	public static final CustomPayload.Id<ShowElectroChargeS2CPayload> ID = new CustomPayload.Id<>(
		SevenElements.identifier("s2c/show_electro_charged")
	);

	public static final PacketCodec<RegistryByteBuf, ShowElectroChargeS2CPayload> CODEC = PacketCodec.tuple(
		PacketCodecs.INTEGER, ShowElectroChargeS2CPayload::mainEntity,
		PacketCodecs.INTEGER.collect(PacketCodecs.toList()), ShowElectroChargeS2CPayload::otherEntities,
		ShowElectroChargeS2CPayload::new
	);

	public ShowElectroChargeS2CPayload(LivingEntity mainEntity, List<LivingEntity> otherEntities) {
		this(mainEntity.getId(), otherEntities.stream().map(LivingEntity::getId).collect(Collectors.toList()));
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
