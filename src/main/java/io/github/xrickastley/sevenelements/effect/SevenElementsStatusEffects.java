package io.github.xrickastley.sevenelements.effect;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class 	SevenElementsStatusEffects {
	/**
	 * Freezes the entity, preventing movement and attacks.
	 */
	public static final StatusEffect FROZEN = register("frozen", new FrozenStatusEffect());
	/**
	 * Reduces the entity's Physical RES% by 40%.
	 */
	public static final StatusEffect SUPERCONDUCT = register("superconduct", new SuperconductStatusEffect());
	/**
	 * Reduces the entity's Movement Speed and Attack Speed by 15%.
	 */
	public static final StatusEffect CRYO = register("cryo", new CryoStatusEffect());

	public static void register() {}

	private static StatusEffect register(String name, StatusEffect statusEffect) {
		return Registry.register(Registries.STATUS_EFFECT, SevenElements.identifier(name), statusEffect);
	}
}
