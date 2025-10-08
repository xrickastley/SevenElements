package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
	@ModifyReturnValue(
		method = "createArrow",
		at = @At(
			value = "RETURN",
			ordinal = 0
		)
	)
	private static PersistentProjectileEntity addInfusionToFireworkRocket(PersistentProjectileEntity original, @Local(ordinal = 0, argsOnly = true) ItemStack weaponStack) {
		original.sevenelements$setOriginStack(weaponStack);

		return original;
	}

	@WrapOperation(
		method = "shoot",
		at = @At(
			value = "NEW",
			args = "class=net/minecraft/entity/projectile/FireworkRocketEntity"
		)
	)
	private static FireworkRocketEntity addInfusionToFireworkRocket(World world, ItemStack stack, Entity entity, double x, double y, double z, boolean shotAtAngle, Operation<FireworkRocketEntity> original, @Local(ordinal = 0, argsOnly = true) ItemStack crossbow) {
		final FireworkRocketEntity firework = original.call(world, stack, entity, x, y, z, shotAtAngle);

		firework.sevenelements$setOriginStack(crossbow);

		return firework;
	}
}
