package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementalAttunementComponent;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayer;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin {
	@WrapOperation(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/item/ItemRenderer;getDirectItemGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;ZZ)Lnet/minecraft/client/render/VertexConsumer;"
		)
	)
	private VertexConsumer renderAttunementGlint(VertexConsumerProvider provider, RenderLayer layer, boolean solid, boolean glint, Operation<VertexConsumer> original, @Local(argsOnly = true) ItemStack itemStack) {
		if (!itemStack.contains(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT)) return original.call(provider, layer, solid, glint);

		final ElementalAttunementComponent elementalAttunement = itemStack.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT);
		final Identifier glintPath = SevenElements.identifier("textures/misc/" + elementalAttunement.element().getId().getPath() + "_enchanted_glint_item.png");

		return VertexConsumers.union(provider.getBuffer(SevenElementsRenderLayer.getElementGlint(glintPath)), original.call(provider, layer, solid, glint));
	}
}
