package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.renderer.ElementGlintRenderer;

import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;

@Debug(export = true)
@Mixin(ItemFeatureRenderer.class)
public abstract class ItemFeatureRendererMixin extends RenderTypeFeatureRenderer<ItemFeatureRenderer.Submit> {
	@Shadow
	@Final
	private QuadInstance quadInstance;

	@Definition(id = "foilType", local = @Local(type = ItemStackRenderState.FoilType.class))
	@Expression("foilType != ?")
	@Inject(
		method = "prepareFoilSubmit",
		at = @At(
			value = "MIXINEXTRAS:EXPRESSION",
			ordinal = 0
		)
	)
	private void renderElementalGlint$1(ItemFeatureRenderer.Submit submit, CallbackInfo ci, @Local ItemStackRenderState.FoilType foilType) {
		if (submit.sevenelements$hasElementalGlint() && !submit.sevenelements$hasAttunementGlint()) {
			for (final BakedQuad quad : submit.quads()) {
				ElementGlintRenderer.STATIC_GLINT
					.getVertexConsumer(this::getVertexBuilder, submit)
					.putBakedQuad(submit.pose(), quad, this.quadInstance);
			}
		}
	}

	@WrapOperation(
		method = "prepareFoilSubmit",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer;getFoilBuffer(Lnet/minecraft/client/renderer/rendertype/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
		)
	)
	private VertexConsumer renderElementalGlint$2(ItemFeatureRenderer instance, RenderType renderType, PoseStack.Pose foilDecalPose, Operation<VertexConsumer> original, @Local(argsOnly = true) ItemFeatureRenderer.Submit submit, @Local BakedQuad quad) {
		return submit.sevenelements$hasAttunementGlint()
			? ElementGlintRenderer.GLINT.getVertexConsumer(this::getVertexBuilder, submit)
			: original.call(instance, renderType, foilDecalPose);
	}
}
