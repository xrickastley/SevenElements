package io.github.xrickastley.sevenelements.networking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistries;

import net.minecraft.util.math.Vec3d;

public record ShowElementalReactionS2CPayload(Vec3d pos, ElementalReaction reaction) implements SevenElementsPayload {
	public static final Codec<ShowElementalReactionS2CPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Vec3d.CODEC.fieldOf("pos").forGetter(ShowElementalReactionS2CPayload::pos),
		SevenElementsRegistries.ELEMENTAL_REACTION.getCodec().fieldOf("reaction").forGetter(ShowElementalReactionS2CPayload::reaction)
	).apply(instance, ShowElementalReactionS2CPayload::new));

	public static final SevenElementsPayload.Id<ShowElementalReactionS2CPayload> ID = new SevenElementsPayload.Id<>(
		SevenElements.identifier("s2c/show_elemental_reaction"),
		ShowElementalReactionS2CPayload.CODEC
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
