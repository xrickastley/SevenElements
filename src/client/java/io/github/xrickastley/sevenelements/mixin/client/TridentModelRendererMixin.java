package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.xrickastley.sevenelements.renderer.ElementGlintRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.render.item.model.special.TridentModelRenderer;

import org.spongepowered.asm.mixin.injection.At;

@Mixin(TridentModelRenderer.class)
public class TridentModelRendererMixin {
	@WrapOperation(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/item/ItemRenderer;getItemGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;ZZ)Lnet/minecraft/client/render/VertexConsumer;"
		)
	)
	private static VertexConsumer renderAttunementGlint$2(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint, Operation<VertexConsumer> original) {
		// this is so ahh :sob:
		if (!glint && !ItemRenderState.Glint.NONE.sevenelements$hasElementalGlint())
			return original.call(vertexConsumers, layer, solid, glint);

		final ItemRenderState.Glint glintState = !glint && ItemRenderState.Glint.NONE.sevenelements$hasElementalGlint()
			? ItemRenderState.Glint.NONE // no glint set
			: ItemRenderState.Glint.STANDARD; // only glint set for SpecialItemModel

		// SevenElements.sublogger().info("Getting item glint consumer!");
		return ElementGlintRenderer.getItemGlintConsumer(original.call(vertexConsumers, layer, solid, glint), vertexConsumers, layer, solid, glint, glintState);
	}
}
