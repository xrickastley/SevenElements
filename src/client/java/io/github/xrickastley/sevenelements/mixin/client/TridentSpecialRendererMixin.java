package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.interfaces.ElementGlintAccess;
import io.github.xrickastley.sevenelements.interfaces.ElementGlintState;
import io.github.xrickastley.sevenelements.renderer.ElementGlintRenderer;

import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.TridentSpecialRenderer;
import net.minecraft.util.Unit;

@Mixin(TridentSpecialRenderer.class)
public class TridentSpecialRendererMixin implements ElementGlintAccess {
	@Unique
	private @Nullable ElementGlintState sevenelements$elementGlintState;

	@Shadow
	@Final
	private TridentModel model;

	@Definition(id = "hasFoil", local = @Local(type = boolean.class))
	@Expression("hasFoil")
	@Inject(
		method = "submit",
		at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private void submitElementalGlint(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor, CallbackInfo ci) {
		if (this.sevenelements$elementGlintState != null
			&& this.sevenelements$elementGlintState.sevenelements$hasElementalGlint()
			&& !this.sevenelements$elementGlintState.sevenelements$hasAttunementGlint()
		) {
			submitNodeCollector.order(1).submitModel(this.model, Unit.INSTANCE, poseStack, ElementGlintRenderer.STATIC_ENTITY_GLINT.getLayer(this.sevenelements$elementGlintState), lightCoords, overlayCoords, outlineColor, null);
		}
	}
	@ModifyArg(
		method = "submit",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;order(I)Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;",
			ordinal = 1
		),
		index = 0
	)
	private int incrementIfElementalGlintSubmitted(int order) {
		return this.sevenelements$elementGlintState != null && this.sevenelements$elementGlintState.sevenelements$hasElementalGlint() && !this.sevenelements$elementGlintState.sevenelements$hasAttunementGlint()
			? order + 1
			: order;
	}

	@ModifyExpressionValue(
		method = "submit",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;entityGlint()Lnet/minecraft/client/renderer/rendertype/RenderType;"
		)
	)
	private RenderType submitAttunementGlint(RenderType original) {
		return this.sevenelements$elementGlintState != null && this.sevenelements$elementGlintState.sevenelements$hasAttunementGlint()
			? ElementGlintRenderer.ENTITY_GLINT.getLayer(this.sevenelements$elementGlintState)
			: original;
	}

	@Unique
	@Override
	public void sevenelements$setElementGlintState(ElementGlintState state) {
		this.sevenelements$elementGlintState = state;
	}
}
