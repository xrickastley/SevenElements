package io.github.xrickastley.sevenelements.mixin.client;

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

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.TridentEntityModel;
import net.minecraft.client.render.item.model.special.TridentModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;

@Mixin(TridentModelRenderer.class)
public class TridentModelRendererMixin implements ElementGlintAccess {
	@Unique
	private @Nullable ElementGlintState sevenelements$elementGlintState;

	@Shadow
	@Final
	private TridentEntityModel model;

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitModelPart(Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IILnet/minecraft/client/texture/Sprite;ZZILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;I)V"
		)
	)
	private void submitAttunementGlint(ItemDisplayContext displayContext, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, boolean glint, int i, CallbackInfo ci) {
		if (this.sevenelements$elementGlintState == null) return;

		AbstractDataAttachments
			.getAttachments(this.model.getRootPart())
			.addAttachment(SevenElements.identifier("element_glint"), this.sevenelements$elementGlintState);
	}

	@Unique
	@Override
	public void sevenelements$setElementGlintState(ElementGlintState state) {
		this.sevenelements$elementGlintState = state;
	}
}
