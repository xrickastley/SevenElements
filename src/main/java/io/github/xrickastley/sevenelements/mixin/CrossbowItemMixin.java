package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
	@ModifyReturnValue(
		method = "createArrowEntity",
		at = @At(
			value = "RETURN",
			ordinal = 0
		)
	)
	private ProjectileEntity addInfusionToFireworkRocket(ProjectileEntity original, @Local(ordinal = 0, argsOnly = true) ItemStack weaponStack) {
		original.sevenelements$setOriginStack(weaponStack);

		return original;
	}
}
