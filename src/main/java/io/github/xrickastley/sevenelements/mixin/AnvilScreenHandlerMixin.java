package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.Text;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {
	@WrapOperation(
		method = "updateResult",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;"
		)
	)
	private Text useTrueNameForRename(ItemStack instance, Operation<Text> original) {
		return instance.sevenelements$getTrueName();
	}
}
