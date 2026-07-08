package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;

@Mixin(ArrowItem.class)
public class ArrowItemMixin {
	@ModifyReturnValue(
		method = "createArrow",
		at = @At("RETURN")
	)
	private PersistentProjectileEntity applyInfusionToProjectile(PersistentProjectileEntity arrow, @Local(argsOnly = true) ItemStack itemStack, @Local(argsOnly = true) LivingEntity shooter) {
		arrow.sevenelements$setOriginStack(shooter.getMainHandStack());
		arrow.sevenelements$setProjectileStack(itemStack);

		return arrow;
	}
}
