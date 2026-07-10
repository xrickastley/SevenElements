package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {
	@WrapOperation(
		method = { "onSlotUpdate", "onRenamed" },
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;"
		)
	)
	private Text useTrueNameForRename(ItemStack instance, Operation<Text> original) {
		return instance.sevenelements$getTrueName();
	}
}
