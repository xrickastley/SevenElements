package io.github.xrickastley.sevenelements.registry;

import com.mojang.serialization.Lifecycle;

import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;

public final class SevenElementsRegistries {
	public static final Registry<ElementalReaction> ELEMENTAL_REACTION = createRegistry(SevenElementsRegistryKeys.ELEMENTAL_REACTION);

	public static void load() {}

	private static <T> Registry<T> createRegistry(RegistryKey<Registry<T>> registryKey) {
		return FabricRegistryBuilder
			.from(new SimpleRegistry<>(registryKey, Lifecycle.stable(), true))
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();
	}
}
