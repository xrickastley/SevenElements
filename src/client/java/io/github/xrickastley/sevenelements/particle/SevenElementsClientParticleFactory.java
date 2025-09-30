package io.github.xrickastley.sevenelements.particle;

import io.github.xrickastley.sevenelements.factory.SevenElementsParticleTypes;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class SevenElementsClientParticleFactory {
	public static void register() {
		ParticleFactoryRegistry
			.getInstance()
			.register(SevenElementsParticleTypes.TINTED_LEAVES, LeavesParticle.TintedLeavesFactory::new);
	}
}
