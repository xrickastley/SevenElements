package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.interfaces.ElementGlintAccess;
import io.github.xrickastley.sevenelements.interfaces.ElementGlintState;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;

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
			target = "Lnet/minecraft/client/render/item/model/special/SpecialModelRenderer;render(Ljava/lang/Object;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IIZI)V"
		)
	)
	private void applyGlint$1(SpecialModelRenderer<?> instance, @Nullable T data, ItemDisplayContext displayContext, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, boolean glint, int i, Operation<Void> original) {
		((ElementGlintAccess) instance).sevenelements$setElementGlintState(this);
		original.call(instance, data, displayContext, matrices, queue, light, overlay, glint, i);
		((ElementGlintAccess) instance).sevenelements$setElementGlintState(null);
	}

	@WrapOperation(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V"
		)
	)
	private void applyGlint$2(OrderedRenderCommandQueue instance, MatrixStack matrices, ItemDisplayContext displayContext, int light, int overlay, int outlineColors, int[] tintLayers, List<BakedQuad> quads, RenderLayer renderLayer, ItemRenderState.Glint glintType, Operation<Void> original) {
		this.sevenelements$applyElementGlintState(this.glint);
		original.call(instance, matrices, displayContext, light, overlay, outlineColors, tintLayers, quads, renderLayer, glintType);
		this.glint.sevenelements$resetElementGlintState();
	}
}
