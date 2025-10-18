package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.entity.CrystallizeShardEntity;
import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

// Prioritized since Frozen **MUST** disable movement.
@Mixin(value = Entity.class, priority = Integer.MIN_VALUE)
public abstract class EntityMixin {
	@Shadow
	public abstract World getEntityWorld();

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

	@Final
	@ModifyArg(
		method = "onStruckByLightning",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z"
		)
	)
	private DamageSource applyElectroOnLightning(DamageSource source, @Local(argsOnly = true) ServerWorld world) {
		return (Entity)(Object) this instanceof final LivingEntity entity && world.getGameRules().getBoolean(SevenElementsGameRules.ELECTRO_FROM_LIGHTNING)
			? new ElementalDamageSource(source, ElementalApplications.gaugeUnits(entity, Element.ELECTRO, 2.0), InternalCooldownContext.ofType(null, "seven-elements:natural_environment", InternalCooldownType.INTERVAL_ONLY).forced())
			: source;
	}

	@Inject(
		method = "setPos",
		at = @At("TAIL")
	)
	private void syncOnPosChangeIfCrystallizeShard(double x, double y, double z, CallbackInfo ci) {
		final CrystallizeShardEntity crystallizeShard = ClassInstanceUtil.castOrNull(this, CrystallizeShardEntity.class);

		if (crystallizeShard == null) return;

		// Sync after pos change, that way PlayerTracking.lookup properly works.
		crystallizeShard.syncToPlayers();
	}
}
