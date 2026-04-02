package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;

import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(GrindstoneMenu.class)
public class GrindstoneMenuMixin {
	@Inject(
		method = "removeNonCursesFrom",
		at = @At("RETURN")
	)
	private void includeElementInGrind(ItemStack item, CallbackInfoReturnable<ItemStack> cir) {
		ElementalInfusionComponent.removeInfusion(item);
	}

	@ModifyExpressionValue(
		method = "computeResult",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;hasAnyEnchantments(Lnet/minecraft/world/item/ItemStack;)Z"
		)
	)
	private boolean checkForElementInResult(boolean original, @Local(ordinal = 2) ItemStack stack) {
		return original || ElementalInfusionComponent.hasInfusion(stack);
	}

	@ModifyReturnValue(
		method = "computeResult",
		at = @At(
			value = "RETURN",
			ordinal = 1
		)
	)
	private ItemStack checkForElementInStacks(ItemStack original, @Local(argsOnly = true, ordinal = 0) ItemStack firstInput, @Local(argsOnly = true, ordinal = 1) ItemStack secondInput) {
		return (ItemStack.isSameItem(firstInput, secondInput) || !(firstInput.isEmpty() == secondInput.isEmpty()))
			&& (firstInput.getCount() + secondInput.getCount() <= 64)
			? new ItemStack(firstInput.getItem(), firstInput.getCount() + secondInput.getCount())
			: original;
	}
}
