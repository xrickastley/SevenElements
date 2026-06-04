package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.component.ItemAttributeModifiers;

@Mixin(ItemAttributeModifiers.Display.Default.class)
public class ItemAttributeModifiers$Display$DefaultMixin {
	@ModifyExpressionValue(
		method = "apply",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;id()I"
		)
	)
	private int modifyIdForMultiplicativeLikeAttributes(int original, @Local(argsOnly = true) Holder<Attribute> attribute) {
		return SevenElementsAttributes.isMultiplicativeLikeAttribute(attribute)
			? 1
			: original;
	}
}
