package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.interfaces.ElementGlintAccess;
import io.github.xrickastley.sevenelements.interfaces.ElementGlintState;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public class ItemStackRenderState$LayerRenderStateMixin<T> implements ElementGlintState {
	@Shadow
	private ItemStackRenderState.FoilType foilType;

	@Inject(
		method = "clear",
		at = @At("TAIL")
	)
	private void clearGlint(CallbackInfo ci) {
		this.sevenelements$resetElementGlintState();
	}

	@WrapOperation(
		method = "submit",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/special/SpecialModelRenderer;submit(Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V"
		)
	)
	private void applyGlint$1(SpecialModelRenderer<?> instance, @Nullable T argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, final int outlineColor, Operation<Void> original) {
		((ElementGlintAccess) instance).sevenelements$setElementGlintState(this);
		original.call(instance, argument, poseStack, submitNodeCollector, lightCoords, overlayCoords, hasFoil, outlineColor);
		((ElementGlintAccess) instance).sevenelements$setElementGlintState(null);
	}

	@WrapOperation(
		method = "submit",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"
		)
	)
	private void applyGlint$2(SubmitNodeCollector instance, PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, ItemStackRenderState.FoilType foilType, Operation<Void> original) {
		this.sevenelements$applyElementGlintState(this.foilType);
		original.call(instance, poseStack, displayContext, lightCoords, overlayCoords, outlineColor, tintLayers, quads, foilType);
		this.foilType.sevenelements$resetElementGlintState();
	}
}
