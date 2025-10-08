package io.github.xrickastley.sevenelements.factory;

import com.mojang.serialization.MapCodec;

import java.util.function.Function;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

// Directly ported from Minecraft 1.21.5 to Minecraft 1.21
public class SevenElementsParticleTypes {
	public static final ParticleType<EntityEffectParticleEffect> TINTED_LEAVES = register("tinted_leaves", false, EntityEffectParticleEffect::createCodec, EntityEffectParticleEffect::createPacketCodec);

	public static void register() {}

	private static <T extends ParticleEffect> ParticleType<T> register(String name, boolean alwaysShow, Function<ParticleType<T>, MapCodec<T>> codecGetter, Function<ParticleType<T>, PacketCodec<? super RegistryByteBuf, T>> packetCodecGetter) {
		return Registry.register(Registries.PARTICLE_TYPE, SevenElements.identifier(name), new ParticleType<T>(alwaysShow) {
			public MapCodec<T> getCodec() {
				return codecGetter.apply(this);
			}

			public PacketCodec<? super RegistryByteBuf, T> getPacketCodec() {
				return packetCodecGetter.apply(this);
			}
		});
	}
}
