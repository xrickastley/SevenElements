package io.github.xrickastley.sevenelements.interfaces;

import java.util.Optional;

import io.github.xrickastley.sevenelements.element.ElementalDamageSource;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public interface InfusableProjectile {
	default void sevenelements$setOriginStack(ItemStack originStack) {}

	default Optional<ElementalDamageSource> sevenelements$attemptInfusion(DamageSource source, Entity target) {
		return Optional.empty();
	}
}
