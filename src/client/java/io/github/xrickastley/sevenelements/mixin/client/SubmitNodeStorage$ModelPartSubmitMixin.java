package io.github.xrickastley.sevenelements.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.interfaces.DataAttachmentAccess;
import io.github.xrickastley.sevenelements.util.AbstractDataAttachments;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Mixin(SubmitNodeStorage.ModelPartSubmit.class)
public class SubmitNodeStorage$ModelPartSubmitMixin implements DataAttachmentAccess {
	@Unique
	public @Nullable AbstractDataAttachments.ReadView sevenelements$attachment;

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void getAttachment(PoseStack.Pose pose, ModelPart modelPart, int lightCoords, int overlayCoords, TextureAtlasSprite sprite, boolean sheeted, boolean hasFoil, int tintedColor, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, int outlineColor, CallbackInfo ci) {
		this.sevenelements$attachment = AbstractDataAttachments.getAttachmentsReadView(modelPart);
	}

	@Override
	public @Nullable AbstractDataAttachments.ReadView sevenelements$getAttachments() {
		return this.sevenelements$attachment;
	}
}
