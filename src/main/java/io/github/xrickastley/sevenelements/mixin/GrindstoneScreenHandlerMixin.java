package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;

@Debug(export = true)
@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneScreenHandlerMixin {
	@Inject(
		method = "grind",
		at = @At("RETURN")
	)
	private void includeElementInGrind(ItemStack item, int damage, int amount, CallbackInfoReturnable<ItemStack> cir) {
		ElementalInfusionComponent.removeInfusion(item);
	}

	@WrapOperation(
		method = "updateResult",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/ItemStack;hasEnchantments()Z"
		)
	)
	private boolean checkForElementInResult(ItemStack instance, Operation<Boolean> original) {
		return original.call(instance) || ElementalInfusionComponent.hasInfusion(instance);
	}

	@ModifyVariable(
		method = "updateResult",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V",
			ordinal = 3
		),
		ordinal = 1
	)
	private ItemStack checkForElementInStacks(ItemStack original, @Local(ordinal = 0) ItemStack firstInput, @Local(ordinal = 1) ItemStack secondInput) {
		return (ItemStack.areItemsEqual(firstInput, secondInput) || !(firstInput.isEmpty() == secondInput.isEmpty()))
			&& (firstInput.getCount() + secondInput.getCount() <= 64)
			? new ItemStack(firstInput.getItem(), firstInput.getCount() + secondInput.getCount())
			: original;
	}
}
