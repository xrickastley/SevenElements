package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(value = Player.class, priority = Integer.MIN_VALUE)
public abstract class PrioritizedPlayerMixin extends LivingEntity {
	public PrioritizedPlayerMixin(final Level world, final BlockPos pos, final float yaw, final GameProfile gameProfile) {
		super(EntityType.PLAYER, world);

		throw new AssertionError();
	}

	@Inject(
		method = "blockActionRestricted",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE // Prioritized since Frozen **MUST** disable movements and actions.
	)
	private void frozenPreventsBreakingBlocks(CallbackInfoReturnable<Boolean> info) {
		if (this.hasEffect(SevenElementsStatusEffects.FROZEN)) info.setReturnValue(true);
	}

	@ModifyExpressionValue(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;createAttackSource(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/damagesource/DamageSource;"
		)
	)
	public DamageSource applyPlayerElementalInfusions(DamageSource source, @Local(argsOnly = true) Entity target) {
		return target instanceof final LivingEntity livingTarget
			? ElementComponent.applyElementalInfusions(source, livingTarget).shouldInfuse(false)
			: source;
	}

	@ModifyExpressionValue(
		method = "stabAttack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;createAttackSource(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/damagesource/DamageSource;"
		)
	)
	private DamageSource applyPlayerElementalInfusionsOnPierce(DamageSource source, @Local(argsOnly = true) Entity target, @Local ItemStack itemStack) {
		final ElementalInfusionComponent component = itemStack.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);

		if (component == null) return source;

		final Optional<ElementalDamageSource> infusedSource = component.apply(source, target);

		return infusedSource.isPresent()
			? infusedSource.get().shouldInfuse(false)
			: source;
	}
}
