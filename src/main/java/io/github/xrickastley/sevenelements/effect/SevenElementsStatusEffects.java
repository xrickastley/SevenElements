package io.github.xrickastley.sevenelements.effect;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class 	SevenElementsStatusEffects {
	/**
	 * Freezes the entity, preventing movement and attacks.
	 */
	public static final RegistryEntry<StatusEffect> FROZEN = register("frozen", new FrozenStatusEffect());
	/**
	 * Reduces the entity's Physical RES% by 40%.
	 */
	public static final RegistryEntry<StatusEffect> SUPERCONDUCT = register("superconduct", new SuperconductStatusEffect());
	/**
	 * Reduces the entity's Movement Speed and Attack Speed by 15%.
	 */
	public static final RegistryEntry<StatusEffect> CRYO = register("cryo", new CryoStatusEffect());

	public static void register() {}

	private static RegistryEntry<StatusEffect> register(String name, StatusEffect statusEffect) {
		return Registry.registerReference(Registries.STATUS_EFFECT, SevenElements.identifier(name), statusEffect);
	}
}
