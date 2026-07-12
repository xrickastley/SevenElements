package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;

import net.minecraft.component.type.BlocksAttacksComponent;
import net.minecraft.entity.LivingEntity;

@Mixin(BlocksAttacksComponent.class)
public class BlocksAttacksComponentMixin {
	@ModifyVariable(
		method = "onShieldHit",
		at = @At("HEAD"),
		argsOnly = true,
		index = 5
	)
	private float applyShieldStrengthAttributeToNormalShield(float amount, @Local(argsOnly = true) LivingEntity entity) {
		return (float) (amount / (1 + (entity.getAttributeValue(SevenElementsAttributes.SHIELD_STRENGTH) / 100)));
	}
}
