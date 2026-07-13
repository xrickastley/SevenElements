package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.interfaces.ElementGlintState;
import io.github.xrickastley.sevenelements.renderer.ElementGlintRenderer;
import io.github.xrickastley.sevenelements.util.AbstractDataAttachments;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelPartFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;

@Mixin(ModelPartFeatureRenderer.class)
public class ModelPartFeatureRendererMixin {
	@ModifyExpressionValue(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelPartSubmit;hasFoil()Z"
		)
	)
	public boolean includeElementalGlints(boolean original, @Local SubmitNodeStorage.ModelPartSubmit modelPartCommand) {
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
			target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer;getFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/rendertype/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
		)
	)
	public VertexConsumer renderAttunementGlint(MultiBufferSource bufferSource, RenderType renderType, boolean sheeted, boolean hasFoil, Operation<VertexConsumer> original, @Local SubmitNodeStorage.ModelPartSubmit modelPartSubmit) {
		final AbstractDataAttachments.ReadView view = modelPartSubmit.sevenelements$getAttachments();
		final ElementGlintState glintState = ClassInstanceUtil.mapOrNull(view, v -> v.getAttachment(SevenElements.identifier("element_glint"), ElementGlintState.class));

		return glintState == null
			? original.call(bufferSource, renderType, sheeted, modelPartSubmit.hasFoil())
			: ElementGlintRenderer.getItemGlintConsumer(original.call(bufferSource, renderType, sheeted, modelPartSubmit.hasFoil()), bufferSource, renderType, sheeted, hasFoil, glintState);
	}
}
