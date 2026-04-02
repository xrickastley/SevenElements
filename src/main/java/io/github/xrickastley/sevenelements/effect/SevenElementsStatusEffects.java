package io.github.xrickastley.sevenelements.effect;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public class 	SevenElementsStatusEffects {
	/**
	 * Freezes the entity, preventing movement and attacks.
	 */
	public static final Holder<MobEffect> FROZEN = register("frozen", new FrozenStatusEffect());
	/**
	 * Reduces the entity's Physical RES% by 40%.
	 */
	public static final Holder<MobEffect> SUPERCONDUCT = register("superconduct", new SuperconductStatusEffect());
	/**
	 * Reduces the entity's Movement Speed and Attack Speed by 15%.
	 */
	public static final Holder<MobEffect> CRYO = register("cryo", new CryoStatusEffect());

	public static void register() {}

	private static Holder<MobEffect> register(String name, MobEffect statusEffect) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, SevenElements.identifier(name), statusEffect);
	}
}
