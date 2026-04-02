package io.github.xrickastley.sevenelements.networking;

import java.util.List;
import java.util.stream.Collectors;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;

public record ShowElectroChargeS2CPayload(int mainEntity, List<Integer> otherEntities) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ShowElectroChargeS2CPayload> ID = new CustomPacketPayload.Type<>(
		SevenElements.identifier("s2c/show_electro_charged")
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ShowElectroChargeS2CPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, ShowElectroChargeS2CPayload::mainEntity,
		ByteBufCodecs.INT.apply(ByteBufCodecs.list()), ShowElectroChargeS2CPayload::otherEntities,
		ShowElectroChargeS2CPayload::new
	);

	public ShowElectroChargeS2CPayload(LivingEntity mainEntity, List<LivingEntity> otherEntities) {
		this(mainEntity.getId(), otherEntities.stream().map(LivingEntity::getId).collect(Collectors.toList()));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
