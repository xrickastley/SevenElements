package io.github.xrickastley.sevenelements.networking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.screen.ElementalInfusionScreenHandler;

public class FinishElementalInfusionS2CPayload implements SevenElementsPayload {
	public static final Codec<FinishElementalInfusionS2CPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("syncId").forGetter(FinishElementalInfusionS2CPayload::syncId)
	).apply(instance, FinishElementalInfusionS2CPayload::new));

	public static final SevenElementsPayload.Id<FinishElementalInfusionS2CPayload> ID = new SevenElementsPayload.Id<>(
		SevenElements.identifier("s2c/finish_elemental_infusion"),
		FinishElementalInfusionS2CPayload.CODEC
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
	public Id<? extends SevenElementsPayload> getId() {
		return ID;
	}

	@Override
	public Codec<? extends SevenElementsPayload> getCodec() {
		return CODEC;
	}
}
