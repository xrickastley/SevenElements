package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;

import net.minecraft.item.ItemStack;

@Mixin(targets = { "net.minecraft.screen.GrindstoneScreenHandler$3", "net.minecraft.screen.GrindstoneScreenHandler$2" })
public class GrindstoneScreenHandler$23Mixin {
	@ModifyReturnValue(
		method = "canInsert(Lnet/minecraft/item/ItemStack;)Z",
		at = @At("RETURN")
	)
	public boolean allowInfusionsForGrindstone(boolean original, @Local(argsOnly = true) ItemStack stack) {
		return original || ElementalInfusionComponent.hasInfusion(stack);
	}
}
