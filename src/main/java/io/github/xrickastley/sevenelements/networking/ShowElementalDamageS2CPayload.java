package io.github.xrickastley.sevenelements.networking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;

import net.minecraft.util.math.Vec3d;

public record ShowElementalDamageS2CPayload(Vec3d pos, Element element, float amount, boolean crit) implements SevenElementsPayload {
	public static final Codec<ShowElementalDamageS2CPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Vec3d.CODEC.fieldOf("pos").forGetter(ShowElementalDamageS2CPayload::pos),
		Element.CODEC.fieldOf("element").forGetter(ShowElementalDamageS2CPayload::element),
		Codec.FLOAT.fieldOf("amount").forGetter(ShowElementalDamageS2CPayload::amount),
		Codec.BOOL.fieldOf("crit").forGetter(ShowElementalDamageS2CPayload::crit)
	).apply(instance, ShowElementalDamageS2CPayload::new));

	public static final SevenElementsPayload.Id<ShowElementalDamageS2CPayload> ID = new SevenElementsPayload.Id<>(
		SevenElements.identifier("s2c/show_elemental_damage"),
		ShowElementalDamageS2CPayload.CODEC
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
