package io.github.xrickastley.sevenelements.registry;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class SevenElementsRegistryKeys {
	public static final ResourceKey<Registry<ElementalReaction>> ELEMENTAL_REACTION = createRegistryKey("elemental_reaction");
	public static final ResourceKey<Registry<InternalCooldownType>> INTERNAL_COOLDOWN_TYPE = createRegistryKey("internal_cooldowns");

	public static void load() {}

	private static <T> ResourceKey<Registry<T>> createRegistryKey(String path) {
		return ResourceKey.createRegistryKey(SevenElements.identifier(path));
	}
}
