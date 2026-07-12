package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.interfaces.ElementGlintState;
import io.github.xrickastley.sevenelements.renderer.ElementGlintRenderer;
import io.github.xrickastley.sevenelements.util.AbstractDataAttachments;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.ModelPartCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;

@Mixin(ModelPartCommandRenderer.class)
public class ModelPartCommandRendererMixin {
	@ModifyExpressionValue(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl$ModelPartCommand;hasGlint()Z"
		)
	)
	public boolean includeElementalGlints(boolean original, @Local OrderedRenderCommandQueueImpl.ModelPartCommand modelPartCommand) {
		final AbstractDataAttachments.ReadView view = modelPartCommand.sevenelements$getAttachments();

		return original
			|| (view != null && view
				.getAttachment(SevenElements.identifier("element_glint"), ElementGlintState.class)
				.sevenelements$hasElementalGlint());
	}

	@WrapOperation(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/item/ItemRenderer;getItemGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;ZZ)Lnet/minecraft/client/render/VertexConsumer;"
		)
	)
	public VertexConsumer renderAttunementGlint(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint, Operation<VertexConsumer> original, @Local OrderedRenderCommandQueueImpl.ModelPartCommand modelPartCommand) {
		final AbstractDataAttachments.ReadView view = modelPartCommand.sevenelements$getAttachments();
		final ElementGlintState glintState = ClassInstanceUtil.mapOrNull(view, v -> v.getAttachment(SevenElements.identifier("element_glint"), ElementGlintState.class));

		return glintState == null
			? original.call(vertexConsumers, layer, solid, modelPartCommand.hasGlint())
			: ElementGlintRenderer.getItemGlintConsumer(original.call(vertexConsumers, layer, solid, modelPartCommand.hasGlint()), vertexConsumers, layer, solid, glint, glintState);
	}
}
