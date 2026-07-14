package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.interfaces.SevenElementsSubmitNodeCollection;

import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.phase.FeatureRenderPhase;

@Mixin(FeatureRenderDispatcher.PreparedFrame.class)
public abstract class FeatureRenderDispatcher$PreparedFrameMixin {
	@Shadow
	protected abstract void executePhase(FeatureRenderPhase<?> phase, FeatureFrameContext context);

	@Inject(
		method = "executeTranslucent",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executePhase(Lnet/minecraft/client/renderer/feature/phase/FeatureRenderPhase;Lnet/minecraft/client/renderer/feature/FeatureFrameContext;)V",
			shift = At.Shift.AFTER,
			ordinal = 5
		)
	)
	private void registerFeatureRenderers(CallbackInfo ci, @Local FeatureFrameContext context, @Local SubmitNodeCollection collection) {
		this.executePhase(((SevenElementsSubmitNodeCollection) collection).sevenelements$getElements(), context);
		this.executePhase(((SevenElementsSubmitNodeCollection) collection).sevenelements$getElementGauges(), context);
		this.executePhase(((SevenElementsSubmitNodeCollection) collection).sevenelements$getCrystallizeShields(), context);
	}
}
