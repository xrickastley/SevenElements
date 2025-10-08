package io.github.xrickastley.sevenelements.networking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.xrickastley.sevenelements.SevenElements;

public record SyncDendroCoreAgeS2CPayload(int entityId, int age) implements SevenElementsPayload {
	public static final Codec<SyncDendroCoreAgeS2CPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("entityId").forGetter(SyncDendroCoreAgeS2CPayload::entityId),
		Codec.INT.fieldOf("age").forGetter(SyncDendroCoreAgeS2CPayload::age)
	).apply(instance, SyncDendroCoreAgeS2CPayload::new));

	public static final SevenElementsPayload.Id<SyncDendroCoreAgeS2CPayload> ID = new SevenElementsPayload.Id<>(
		SevenElements.identifier("s2c/sync_dendro_core_age"),
		SyncDendroCoreAgeS2CPayload.CODEC
	);

	@Override
	public Id<? extends SevenElementsPayload> getId() {
		return ID;
	}

	@Override
	public Codec<? extends SevenElementsPayload> getCodec() {
		return CODEC;
	}
}
