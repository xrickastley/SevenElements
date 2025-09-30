package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.world.World;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin {
	@Inject(
		method = "createArrowEntity",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/item/ArrowItem;createArrow(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;"
		)
	)
	private void applyInfusionToProjectile(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical, CallbackInfoReturnable<ProjectileEntity> cir, @Local PersistentProjectileEntity projectile) {
		projectile.sevenelements$setOriginStack(weaponStack);
	}
}
