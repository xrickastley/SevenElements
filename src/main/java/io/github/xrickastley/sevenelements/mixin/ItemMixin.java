package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.xrickastley.sevenelements.annotation.mixin.Local;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemMixin {
	@ModifyReturnValue(
		method = "hasGlint",
		at = @At("RETURN")
	)
	private boolean includeElementalGlint(boolean original, @Local(argsOnly = true) ItemStack stack) {
		return original
			|| stack.sevenelements$hasAttunementGlint();
	}
}
