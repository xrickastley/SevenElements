package io.github.xrickastley.sevenelements.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.interfaces.ElementGlintState;

import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;

@Mixin(SubmitNodeStorage.ItemSubmit.class)
public class SubmitNodeStorage$ItemSubmitMixin implements ElementGlintState {
	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void getAttachment(PoseStack.Pose pose, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, ItemStackRenderState.FoilType foilType, CallbackInfo ci) {
		foilType.sevenelements$applyElementGlintState(this);
	}
}
