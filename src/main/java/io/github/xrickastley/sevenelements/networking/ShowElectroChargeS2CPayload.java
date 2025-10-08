package io.github.xrickastley.sevenelements.networking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.stream.Collectors;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.entity.LivingEntity;

public record ShowElectroChargeS2CPayload(int mainEntity, List<Integer> otherEntities) implements SevenElementsPayload {
	public static final Codec<ShowElectroChargeS2CPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("mainEntity").forGetter(ShowElectroChargeS2CPayload::mainEntity),
		Codec.INT.listOf().fieldOf("otherEntities").forGetter(ShowElectroChargeS2CPayload::otherEntities)
	).apply(instance, ShowElectroChargeS2CPayload::new));

	public static final SevenElementsPayload.Id<ShowElectroChargeS2CPayload> ID = new SevenElementsPayload.Id<>(
		SevenElements.identifier("s2c/show_electro_charged"),
		ShowElectroChargeS2CPayload.CODEC
	);

	public ShowElectroChargeS2CPayload(LivingEntity mainEntity, List<LivingEntity> otherEntities) {
		this(mainEntity.getId(), otherEntities.stream().map(LivingEntity::getId).collect(Collectors.toList()));
	}

	@Override
	public Codec<? extends SevenElementsPayload> getCodec() {
		return CODEC;
	}

	@Override
	public Id<? extends SevenElementsPayload> getId() {
		return ID;
	}
}
