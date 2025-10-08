package io.github.xrickastley.sevenelements.util.dynamic;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.List;
import java.util.function.Function;

import org.joml.Vector4f;

import io.github.xrickastley.sevenelements.particle.EntityEffectParticleEffect;

import net.minecraft.util.Util;

// Ported some fields from Minecraft 1.21.5 to Minecraft 1.20.1
public class SevenElementsCodecs {
	static <T, U> Codec<T> withAlternative(final Codec<T> primary, final Codec<U> alternative, final Function<U, T> converter) {
		return Codec.either(
			primary,
			alternative
		).xmap(
			either -> either.map(v -> v, converter),
			Either::left
		);
	}

	public static final Codec<Vector4f> VECTOR_4F = Codec.FLOAT
		.listOf()
		.comapFlatMap(
			list -> Util
				.decodeFixedLengthList(list, 4)
				.map(listx -> new Vector4f(listx.get(0), listx.get(1), listx.get(2), listx.get(3))),
			vec4f -> List.of(vec4f.x(), vec4f.y(), vec4f.z(), vec4f.w())
		);

	public static final Codec<Integer> ARGB = SevenElementsCodecs.withAlternative(
		Codec.INT, SevenElementsCodecs.VECTOR_4F,
		vec4f -> EntityEffectParticleEffect.fromFloats(vec4f.w(), vec4f.x(), vec4f.y(), vec4f.z())
	);
}
