package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.resources.model.geometry.BakedQuad;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import io.github.xrickastley.sevenelements.renderer.ElementGlintRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFeatureRenderer.class)
public class ItemFeatureRendererMixin {
	@Shadow
	@Final
	private QuadInstance quadInstance;

	@Definition(id = "foilType", local = @Local(type = ItemStackRenderState.FoilType.class))
	@Expression("foilType != ?")
	@Inject(
		method = "renderItem",
		at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private void renderElementalGlint$1(MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, SubmitNodeStorage.ItemSubmit submit, CallbackInfo ci, @Local ItemStackRenderState.FoilType foilType, @Local BakedQuad quad) {
		if (foilType == ItemStackRenderState.FoilType.NONE && submit.sevenelements$hasElementalGlint()) {
			ElementGlintRenderer
				.getItemGlintConsumer(bufferSource, quad.materialInfo().itemRenderType(), true, false, submit)
				.putBakedQuad(submit.pose(), quad, this.quadInstance);
		}
	}

	@WrapOperation(
		method = "renderItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer;getFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/rendertype/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
		)
	)
	private VertexConsumer renderElementalGlint$2(MultiBufferSource bufferSource, RenderType renderType, PoseStack.Pose foilDecalPose, Operation<VertexConsumer> original, @Local(argsOnly = true) SubmitNodeStorage.ItemSubmit submit) {
		return submit.sevenelements$hasElementalGlint()
			? ElementGlintRenderer.getItemGlintConsumer(original.call(bufferSource, renderType, foilDecalPose), bufferSource, renderType, true, submit.foilType() != ItemStackRenderState.FoilType.NONE, submit)
			: original.call(bufferSource, renderType, foilDecalPose);
	}
}
