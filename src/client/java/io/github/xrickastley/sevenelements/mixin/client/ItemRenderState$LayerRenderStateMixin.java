package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import io.github.xrickastley.sevenelements.interfaces.ElementGlintState;

import net.minecraft.client.render.item.ItemRenderState;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

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
			target = "Lnet/minecraft/client/render/item/model/special/SpecialModelRenderer;render(Ljava/lang/Object;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V"
		)
	)
	private void applyGlint$1(SpecialModelRenderer<?> instance, @Nullable T data, ItemDisplayContext displayContext, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, boolean glint, Operation<Void> original) {
		this.sevenelements$applyElementGlintState(this.glint);
		original.call(instance, data, displayContext, matrices, vertexConsumers, light, overlay, glint);
		this.glint.sevenelements$resetElementGlintState();
	}

	@WrapOperation(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II[ILjava/util/List;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V"
		)
	)
	private void applyGlint$2(ItemDisplayContext displayContext, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, int[] tints, List<BakedQuad> quads, RenderLayer layer, ItemRenderState.Glint glint, Operation<Void> original) {
		this.sevenelements$applyElementGlintState(this.glint);
		original.call(displayContext, matrices, vertexConsumers, light, overlay, tints, quads, layer, glint);
		this.glint.sevenelements$resetElementGlintState();
	}
}
