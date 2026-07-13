package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.annotation.mixin.Local;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(Item.class)
public class ItemMixin {
	@ModifyReturnValue(
		method = "isFoil",
		at = @At("RETURN")
	)
	private boolean includeElementalGlint(boolean original, @Local(argsOnly = true) ItemStack stack) {
		return original
			|| stack.sevenelements$hasAttunementGlint();
	}
}
