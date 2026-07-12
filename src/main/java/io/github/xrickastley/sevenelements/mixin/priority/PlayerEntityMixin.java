package io.github.xrickastley.sevenelements.mixin.priority;

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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(value = PlayerEntity.class, priority = Integer.MIN_VALUE)
public abstract class PlayerEntityMixin extends LivingEntity {
	public PlayerEntityMixin(final World world, final BlockPos pos, final float yaw, final GameProfile gameProfile) {
		super(EntityType.PLAYER, world);

		throw new AssertionError();
	}

	@Inject(
		method = "isBlockBreakingRestricted",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsBreakingBlocks(CallbackInfoReturnable<Boolean> info) {
		if (this.hasStatusEffect(SevenElementsStatusEffects.FROZEN)) info.setReturnValue(true);
	}

	// Infusions should be considered ASAP.
	@ModifyExpressionValue(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/player/PlayerEntity;getDamageSource(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/damage/DamageSource;"
		)
	)
	public DamageSource applyPlayerElementalInfusions(DamageSource source, @Local(argsOnly = true) Entity target) {
		return target instanceof final LivingEntity livingTarget
			? ElementComponent.applyElementalInfusions(source, livingTarget).shouldInfuse(false)
			: source;
	}

	@ModifyExpressionValue(
		method = "pierce",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/player/PlayerEntity;getDamageSource(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/damage/DamageSource;"
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
