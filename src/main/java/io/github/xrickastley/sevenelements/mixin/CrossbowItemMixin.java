package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
	@ModifyReturnValue(
		method = "createProjectile",
		at = @At(
			value = "RETURN",
			ordinal = 0
		)
	)
	private Projectile addInfusionToFireworkRocket(Projectile original, @Local(ordinal = 0, argsOnly = true) ItemStack weaponStack) {
		original.sevenelements$setOriginStack(weaponStack);

		return original;
	}
}
