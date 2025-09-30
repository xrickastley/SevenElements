package io.github.xrickastley.sevenelements.interfaces;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public interface ILivingEntity {
	/**
	 * Gets the "planned" attacker. This is updated at the <i>very</i> start of the damage method,
	 * meaning that the attacker may not be able to attack after all succeeding conditions are
	 * checked. <br> <br>
	 *
	 * This is the attacker of the <i>most recent</i> {@code DamageSource} passed through
	 * {@link LivingEntity#damage LivingEntity#damage}.
	 *
	 * @see LivingEntity#getAttacker()
	 */
	default @Nullable Entity sevenelements$getPlannedAttacker() {
		return null;
	}

	/**
	 * Gets the "planned" damage source. This is updated at the <i>very</i> start of the damage
	 * method, meaning that the damage source may not be applied after all succeeding conditions
	 * are checked. <br> <br>
	 *
	 * This is the <i>most recent</i> {@code DamageSource} passed through
	 * {@link LivingEntity#damage LivingEntity#damage}.
	 */
	default @Nullable DamageSource sevenelements$getPlannedDamageSource() {
		return null;
	}

	/**
	 * Sets whether the damage was blocked by the Crystallize Shield. <br> <br>
	 *
	 * To <b>only</b> be used by subclasses of {@code LivingEntity} that don't call upon
	 * {@link LivingEntity#applyDamage LivingEntity#applyDamage}.
	 */
	default void sevenelements$setBlockedByCrystallizeShield(boolean blocked) {}
}
