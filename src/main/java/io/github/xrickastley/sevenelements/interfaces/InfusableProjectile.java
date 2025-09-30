package io.github.xrickastley.sevenelements.interfaces;

import java.util.Optional;

import io.github.xrickastley.sevenelements.element.ElementalDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;

public interface InfusableProjectile {
	default void sevenelements$setOriginStack(ItemStack originStack) {}

	default Optional<ElementalDamageSource> sevenelements$attemptInfusion(DamageSource source, Entity target) {
		return Optional.empty();
	}
}
