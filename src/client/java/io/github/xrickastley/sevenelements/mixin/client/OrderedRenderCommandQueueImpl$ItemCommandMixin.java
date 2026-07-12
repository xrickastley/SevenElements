package io.github.xrickastley.sevenelements.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.interfaces.ElementGlintState;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;

@Mixin(OrderedRenderCommandQueueImpl.ItemCommand.class)
public class OrderedRenderCommandQueueImpl$ItemCommandMixin implements ElementGlintState {
	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void getAttachment(MatrixStack.Entry positionMatrix, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, RenderLayer renderLayer, ItemRenderState.Glint glintType, CallbackInfo ci) {
		glintType.sevenelements$applyElementGlintState(this);
	}
}
