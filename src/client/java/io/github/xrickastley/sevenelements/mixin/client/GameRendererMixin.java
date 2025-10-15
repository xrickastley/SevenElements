package io.github.xrickastley.sevenelements.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderer;
import net.minecraft.client.render.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(
		method = "close",
		at = @At("TAIL")
	)
	private void closeAllocators(CallbackInfo ci) {
		SevenElementsRenderer.close();
	}
}
