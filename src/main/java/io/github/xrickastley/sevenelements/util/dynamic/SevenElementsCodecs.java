package io.github.xrickastley.sevenelements.util.dynamic;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.joml.Vector4f;

import io.github.xrickastley.sevenelements.particle.EntityEffectParticleEffect;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryOps.RegistryInfo;
import net.minecraft.registry.RegistryOps.RegistryInfoGetter;
import net.minecraft.util.Util;

// Custom utilities + Ported some fields from Minecraft 1.21.5 to Minecraft 1.20.1
public class SevenElementsCodecs {
	public static final RegistryInfoGetter STATIC_REGISTRY_LOOKUP = new StaticRegistryInfoGetter();

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

	private static final class StaticRegistryInfoGetter implements RegistryOps.RegistryInfoGetter {
		private final Map<RegistryKey<? extends Registry<?>>, Optional<? extends RegistryOps.RegistryInfo<?>>> cache = new HashMap<>();

		@Override
		@SuppressWarnings("unchecked")
		public <T> Optional<RegistryInfo<T>> getRegistryInfo(RegistryKey<? extends Registry<? extends T>> registryRef) {
			return (Optional<RegistryInfo<T>>) cache.computeIfAbsent(
				registryRef, 
				ref -> Optional
					.ofNullable((Registry<T>) Registries.REGISTRIES.get(ClassInstanceUtil.castOrNull(ref, RegistryKey.class)))
					.map(registry -> new RegistryInfo<T>(registry.getEntryOwner(), registry.getReadOnlyWrapper(), registry.getLifecycle()))
			);
		}
	}
}
