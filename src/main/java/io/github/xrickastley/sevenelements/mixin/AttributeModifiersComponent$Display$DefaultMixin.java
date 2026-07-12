package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;

@Mixin(AttributeModifiersComponent.Display.Default.class)
public class AttributeModifiersComponent$Display$DefaultMixin {
	@ModifyExpressionValue(
		method = "addTooltip",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier$Operation;getId()I"
		)
	)
	private int modifyIdForMultiplicativeLikeAttributes(int original, @Local(argsOnly = true) RegistryEntry<EntityAttribute> attribute) {
		return SevenElementsAttributes.isMultiplicativeLikeAttribute(attribute)
			? 1
			: original;
	}
}
