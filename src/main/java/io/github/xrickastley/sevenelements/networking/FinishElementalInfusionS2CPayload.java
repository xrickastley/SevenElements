package io.github.xrickastley.sevenelements.networking;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.screen.ElementalInfusionScreenHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class FinishElementalInfusionS2CPayload implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<FinishElementalInfusionS2CPayload> ID = new CustomPacketPayload.Type<>(
		SevenElements.identifier("s2c/finish_elemental_infusion")
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, FinishElementalInfusionS2CPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, FinishElementalInfusionS2CPayload::syncId,
		FinishElementalInfusionS2CPayload::new
	);

	private final int syncId;

	public FinishElementalInfusionS2CPayload(ElementalInfusionScreenHandler screenHandler) {
		this(screenHandler.containerId);
	}

	private FinishElementalInfusionS2CPayload(int syncId) {
		this.syncId = syncId;
	}

	public int syncId() {
		return syncId;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
