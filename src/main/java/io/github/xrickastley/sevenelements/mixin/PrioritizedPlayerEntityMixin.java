package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(value = PlayerEntity.class, priority = Integer.MIN_VALUE)
public abstract class PrioritizedPlayerEntityMixin extends LivingEntity {
	public PrioritizedPlayerEntityMixin(final World world, final BlockPos pos, final float yaw, final GameProfile gameProfile) {
		super(EntityType.PLAYER, world);

		throw new AssertionError();
	}

	@Inject(
		method = "isBlockBreakingRestricted",
		at = @At("HEAD"),
		cancellable = true
	)
	// Prioritized since Frozen **MUST** disable movements and actions.
	private void frozenPreventsBreakingBlocks(CallbackInfoReturnable<Boolean> info) {
		if (this.hasStatusEffect(SevenElementsStatusEffects.FROZEN)) info.setReturnValue(true);
	}

	@ModifyExpressionValue(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/damage/DamageSources;playerAttack(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/entity/damage/DamageSource;"
		)
	)
	public DamageSource applyPlayerElementalInfusions(DamageSource source, @Local(argsOnly = true) Entity target) {
		return target instanceof final LivingEntity livingTarget
			? ElementComponent.applyElementalInfusions(source, livingTarget).shouldInfuse(false)
			: source;
	}
}
