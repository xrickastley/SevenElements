package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;

import net.minecraft.world.item.ItemStack;

@Mixin(targets = {"net.minecraft.world.inventory.GrindstoneMenu$3", "net.minecraft.world.inventory.GrindstoneMenu$2"})
public class GrindstoneMenu$23Mixin {
	@ModifyReturnValue(
		method = "mayPlace",
		at = @At("RETURN")
	)
	public boolean allowInfusionsForGrindstone(boolean original, @Local(argsOnly = true) ItemStack stack) {
		return original || ElementalInfusionComponent.hasInfusion(stack);
	}
}
