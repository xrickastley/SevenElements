package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;

import net.minecraft.item.ItemStack;

@Mixin(targets = "net.minecraft.screen.GrindstoneScreenHandler$4")
public class GrindstoneScreenHandler$4Mixin {
	@ModifyReturnValue(
		method = "getExperience(Lnet/minecraft/item/ItemStack;)I",
		at = @At("RETURN")
	)
	public int addElementsAsExperience(int original, @Local(argsOnly = true) ItemStack stack) {
		final ElementalInfusionComponent component = ElementalInfusionComponent.get(stack);

		if (component == null || !component.hasElementalInfusion()) return original;

		return original + (int) (60 * component.getGaugeUnits());
	}
}
