package io.github.xrickastley.sevenelements.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.interfaces.DataAttachmentAccess;
import io.github.xrickastley.sevenelements.util.AbstractDataAttachments;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(OrderedRenderCommandQueueImpl.ModelPartCommand.class)
public class OrderedRenderCommandQueueImpl$ModelPartCommandMixin implements DataAttachmentAccess {
	@Unique
	public @Nullable AbstractDataAttachments.ReadView sevenelements$attachment;

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void getAttachment(MatrixStack.Entry entry, ModelPart modelPart, int i, int j, Sprite sprite, boolean bl, boolean bl2, int k, ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlayCommand, int l, CallbackInfo ci) {
		this.sevenelements$attachment = AbstractDataAttachments.getAttachmentsReadView(modelPart);
	}

	@Override
	public @Nullable AbstractDataAttachments.ReadView sevenelements$getAttachments() {
		return this.sevenelements$attachment;
	}
}
