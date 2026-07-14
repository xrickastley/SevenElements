package io.github.xrickastley.sevenelements.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.renderer.feature.CrystallizeShieldFeatureRenderer;
import io.github.xrickastley.sevenelements.renderer.feature.ElementFeatureRenderer;
import io.github.xrickastley.sevenelements.renderer.feature.ElementGaugeFeatureRenderer;

import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.FeatureRendererMap;

@Mixin(FeatureRenderDispatcher.class)
public class FeatureRenderDispatcherMixin {
	@Shadow
	@Final
	private FeatureRendererMap featureRenderers;

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void registerFeatureRenderers(CallbackInfo ci) {
		featureRenderers.put(ElementFeatureRenderer.TYPE, new ElementFeatureRenderer());
		featureRenderers.put(ElementGaugeFeatureRenderer.TYPE, new ElementGaugeFeatureRenderer());
		featureRenderers.put(CrystallizeShieldFeatureRenderer.TYPE, new CrystallizeShieldFeatureRenderer());
	}
}
