package io.github.xrickastley.sevenelements.mixin.priority;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = Integer.MIN_VALUE)
public abstract class EntityMixin {
	@Shadow
	public abstract LivingEntity getControllingPassenger();

	// Frozen **must** disable movements and actions.
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
		ordinal = 0,
		order = Integer.MIN_VALUE // Frozen **must** disable movement.
	)
	private Vec3d frozenPreventsMovement$1(Vec3d original) {
		final @Nullable LivingEntity entity = ClassInstanceUtil.castOrNull(this, LivingEntity.class);
		final @Nullable LivingEntity controller = this.getControllingPassenger();

		return (entity != null && entity.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			|| (controller != null && controller.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			? new Vec3d(0, original.y, 0)
			: original;
	}

	@Inject(
		method = "refreshPositionAndAngles(DDDFF)V",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movement.
	)
	private void frozenPreventsMovement$2(double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
		final @Nullable LivingEntity entity = ClassInstanceUtil.castOrNull(this, LivingEntity.class);
		final @Nullable LivingEntity controller = this.getControllingPassenger();

		if ((entity != null && entity.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			|| (controller != null && controller.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
		) ci.cancel();
	}

	@Inject(
		method = "changeLookDirection",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movement.
	)
	private void frozenPreventsMovement$3(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
		final @Nullable LivingEntity entity = ClassInstanceUtil.castOrNull(this, LivingEntity.class);
		final @Nullable LivingEntity controller = this.getControllingPassenger();

		if ((entity != null && entity.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			|| (controller != null && controller.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
		) ci.cancel();
	}
}
