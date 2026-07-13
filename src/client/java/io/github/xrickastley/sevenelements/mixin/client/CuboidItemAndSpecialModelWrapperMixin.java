package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.renderer.ElementGlintRenderer;

import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.world.item.ItemStack;

@Mixin(value = { CuboidItemModelWrapper.class, SpecialModelWrapper.class })
public class CuboidItemAndSpecialModelWrapperMixin {
	@ModifyExpressionValue(
		method = "update",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;newLayer()Lnet/minecraft/client/renderer/item/ItemStackRenderState$LayerRenderState;"
		)
	)
	private ItemStackRenderState.LayerRenderState addElementDataToLayerState(ItemStackRenderState.LayerRenderState original, @Local(argsOnly = true) ItemStack itemStack, @Local(argsOnly = true) ItemStackRenderState state) {
		if (itemStack.sevenelements$hasElementalGlint()) {
			final ElementGlintRenderer.GlintType glintType = itemStack.sevenelements$hasAttunementGlint()
				? ElementGlintRenderer.GlintType.ATTUNEMENT
				: ElementGlintRenderer.GlintType.INFUSION;

			final Element element = itemStack.sevenelements$hasAttunementGlint()
				? itemStack.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT).element()
				: itemStack.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT).getElement();

			original.sevenelements$setGlintType(glintType);
			original.sevenelements$setElement(element);

			// In GUI rendering, rendering looks to be cached by model keys?, add these elements to them to force a unique render.
			state.appendModelIdentityElement(glintType);
			state.appendModelIdentityElement(element);

			if (itemStack.sevenelements$hasAttunementGlint())
				state.setAnimated();
		}

		return original;
	}
}
