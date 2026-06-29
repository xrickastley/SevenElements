package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.interfaces.IEntity;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

// Prioritized since Frozen **MUST** disable movement.
@Mixin(value = Entity.class, priority = Integer.MIN_VALUE)
public abstract class PrioritizedEntityMixin implements IEntity {
	@ModifyReturnValue(
		method = "handleAttack",
		at = @At("RETURN")
	)
	private boolean noAttackIfAttackerFrozen(boolean original, @Local(argsOnly = true) Entity attacker) {
		final boolean attackerHasFrozenEffect = attacker instanceof final LivingEntity livingAttacker
			&& livingAttacker.hasStatusEffect(SevenElementsStatusEffects.FROZEN);

		return original || attackerHasFrozenEffect;
	}

	@ModifyVariable(
		method = "setVelocity(Lnet/minecraft/util/math/Vec3d;)V",
		at = @At("HEAD"),
		argsOnly = true,
		ordinal = 0
	)
	private Vec3d frozenPreventsMovement(Vec3d original) {
		final @Nullable LivingEntity entity = ClassInstanceUtil.castOrNull(this, LivingEntity.class);

		return entity != null && entity.hasStatusEffect(SevenElementsStatusEffects.FROZEN)
			? new Vec3d(0, original.y, 0)
			: original;
	}
}
