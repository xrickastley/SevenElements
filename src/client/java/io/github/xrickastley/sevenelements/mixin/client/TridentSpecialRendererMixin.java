package io.github.xrickastley.sevenelements.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.interfaces.ElementGlintAccess;
import io.github.xrickastley.sevenelements.interfaces.ElementGlintState;
import io.github.xrickastley.sevenelements.util.AbstractDataAttachments;

import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.TridentSpecialRenderer;

@Mixin(TridentSpecialRenderer.class)
public class TridentSpecialRendererMixin implements ElementGlintAccess {
	@Unique
	private @Nullable ElementGlintState sevenelements$elementGlintState;

	@Shadow
	@Final
	private TridentModel model;

	@Inject(
		method = "submit",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZZILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;I)V"
		)
	)
	private void submitAttunementGlint(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor, CallbackInfo ci) {
		if (this.sevenelements$elementGlintState == null) return;

		AbstractDataAttachments
			.getAttachments(this.model.root())
			.addAttachment(SevenElements.identifier("element_glint"), this.sevenelements$elementGlintState);
	}

	@Unique
	@Override
	public void sevenelements$setElementGlintState(ElementGlintState state) {
		this.sevenelements$elementGlintState = state;
	}
}
