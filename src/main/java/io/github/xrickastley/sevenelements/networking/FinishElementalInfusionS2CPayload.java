package io.github.xrickastley.sevenelements.networking;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.screen.ElementalInfusionScreenHandler;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public class FinishElementalInfusionS2CPayload implements CustomPayload {
	public static final CustomPayload.Id<FinishElementalInfusionS2CPayload> ID = new CustomPayload.Id<>(
		SevenElements.identifier("s2c/finish_elemental_infusion")
	);

	public static final PacketCodec<RegistryByteBuf, FinishElementalInfusionS2CPayload> CODEC = PacketCodec.tuple(
		PacketCodecs.INTEGER, FinishElementalInfusionS2CPayload::syncId,
		FinishElementalInfusionS2CPayload::new
	);

	private final int syncId;

	public FinishElementalInfusionS2CPayload(ElementalInfusionScreenHandler screenHandler) {
		this(screenHandler.syncId);
	}

	private FinishElementalInfusionS2CPayload(int syncId) {
		this.syncId = syncId;
	}

	public int syncId() {
		return syncId;
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
