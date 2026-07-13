package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {
	@WrapOperation(
		method = "createResult",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;"
		)
	)
	private Component useTrueNameForRename(ItemStack instance, Operation<Component> original) {
		return instance.sevenelements$getTrueName();
	}
}
