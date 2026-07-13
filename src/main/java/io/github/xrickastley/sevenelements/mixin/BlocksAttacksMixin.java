package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.BlocksAttacks;

@Mixin(BlocksAttacks.class)
public class BlocksAttacksMixin {
	@ModifyVariable(
		method = "hurtBlockingItem",
		at = @At("HEAD"),
		argsOnly = true,
		index = 5
	)
	private float applyShieldStrengthAttributeToNormalShield(float amount, @Local(argsOnly = true) LivingEntity entity) {
		return (float) (amount / (1 + (entity.getAttributeValue(SevenElementsAttributes.SHIELD_STRENGTH) / 100)));
	}
}
