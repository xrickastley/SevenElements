package io.github.xrickastley.sevenelements.registry;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public final class SevenElementsRegistryKeys {
	public static final RegistryKey<Registry<ElementalReaction>> ELEMENTAL_REACTION = createRegistryKey("elemental_reaction");
	public static final RegistryKey<Registry<InternalCooldownType>> INTERNAL_COOLDOWN_TYPE = createRegistryKey("internal_cooldowns");

	public static void load() {}

	private static <T> RegistryKey<Registry<T>> createRegistryKey(String path) {
		return RegistryKey.ofRegistry(SevenElements.identifier(path));
	}
}
