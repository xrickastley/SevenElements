package io.github.xrickastley.sevenelements.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.entity.effect.StatusEffectInstance;

@Mixin(AbstractInventoryScreen.class)
public class AbstractInventoryScreenMixin {
	@Inject(
		method = "drawStatusEffectSprites",
		at = @At("HEAD")
	)
	private void enableBlend(DrawContext context, int x, int height, Iterable<StatusEffectInstance> statusEffects, boolean wide, CallbackInfo ci) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableCull();
	}

	@Inject(
		method = "drawStatusEffectSprites",
		at = @At("TAIL")
	)
	private void disableBlend(DrawContext context, int x, int height, Iterable<StatusEffectInstance> statusEffects, boolean wide, CallbackInfo ci) {
		RenderSystem.disableBlend();
		RenderSystem.disableCull();
	}
}
