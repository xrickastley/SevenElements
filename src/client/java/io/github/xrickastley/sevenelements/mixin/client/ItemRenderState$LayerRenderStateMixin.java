package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ModelTransformationMode;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import io.github.xrickastley.sevenelements.interfaces.ElementGlintState;

import net.minecraft.client.render.item.ItemRenderState;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderState.LayerRenderState.class)
public class ItemRenderState$LayerRenderStateMixin<T> implements ElementGlintState {
	@Shadow
	private ItemRenderState.Glint glint;

	@Inject(
		method = "clear",
		at = @At("TAIL")
	)
	private void clearGlint(CallbackInfo ci) {
		this.sevenelements$resetElementGlintState();
	}

	@WrapOperation(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/item/model/special/SpecialModelRenderer;render(Ljava/lang/Object;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V"
		)
	)
	private void applyGlint$1(SpecialModelRenderer<Object> instance, @Nullable T t, ModelTransformationMode modelTransformationMode, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, int overlay, boolean b, Operation<Void> original) {
		this.sevenelements$applyElementGlintState(this.glint);
		original.call(instance, t, modelTransformationMode, matrixStack, vertexConsumerProvider, light, overlay, b);
		this.glint.sevenelements$resetElementGlintState();
	}

	@WrapOperation(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II[ILnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V"
		)
	)
	private void applyGlint$2(ModelTransformationMode transformationMode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, int[] tints, BakedModel model, RenderLayer layer, ItemRenderState.Glint glint, Operation<Void> original) {
		this.sevenelements$applyElementGlintState(this.glint);
		original.call(transformationMode, matrices, vertexConsumers, light, overlay, tints, model, layer, glint);
		this.glint.sevenelements$resetElementGlintState();
	}
}
