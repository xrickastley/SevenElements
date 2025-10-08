package io.github.xrickastley.sevenelements.factory;

import io.github.xrickastley.sevenelements.SevenElements;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

// Ported with changes from Minecraft 1.21.5 to Minecraft 1.20.1
public class SevenElementsParticleTypes {
	public static final DefaultParticleType TINTED_LEAVES = register("tinted_leaves");

	public static void register() {}

	private static DefaultParticleType register(String id) {
		return Registry.register(Registries.PARTICLE_TYPE, SevenElements.identifier(id), FabricParticleTypes.simple());
	}
}
