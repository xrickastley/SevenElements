package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderPipelines;
import io.github.xrickastley.sevenelements.util.Array;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.player.Player;

@Mixin(Gui.class)
public class GuiMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private static Identifier POWDER_SNOW_OUTLINE_LOCATION;

	@Shadow
	private void renderTextureOverlay(GuiGraphics context, Identifier texture, float opacity) {
		throw new AssertionError();
	}

	@Inject(
		method = "renderPlayerHealth",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/Gui;renderArmor(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIII)V",
			shift = At.Shift.AFTER
		)
	)
	private void renderAppliedElements(GuiGraphics context, CallbackInfo ci, @Local Player player, @Local(ordinal = 4) int y, @Local(ordinal = 2) int x, @Local(ordinal = 6) int p, @Local(ordinal = 7) int lines) {
		Profiler.get().popPush("seven-elements:elements");

		y -= (p - 1) * lines;

		int offset = 1;

		if (player.getArmorValue() > 0) offset++;

		y -= (10 * (offset));

		final ElementComponent component = ElementComponent.KEY.get(player);

		// final double scaleFactor = MinecraftClient.getInstance().getWindow().getScaleFactor();
		final Set<Identifier> existing = new HashSet<>();
		final Array<Identifier> appliedElements = component
			.getAppliedElements()
			.map(Functions.compose(ElementalApplication::getElement, Element::getTexture))
			.filter(existing::add);

		if (component.getCrystallizeShield() != null && component.getCrystallizeShield().getB() > 0)
			appliedElements.add(SevenElements.identifier("textures/status_effect/defense.png"));

		for (int i = 0; i < appliedElements.length(); i++) {
			final Identifier texture = appliedElements.get(i);
			final int x1 = x + (i * 10);

			context.sevenelements$drawCircle(SevenElementsRenderPipelines.CIRCLE, x1 + 4.5f, y + 4.5f, 4.5f, 0x7F646464);
			context.blit(RenderPipelines.GUI_TEXTURED, texture, x1, y, 0, 0, 9, 9, 9, 9);
		}
	}

	@Inject(
		method = "renderCameraOverlays",
		at = @At("TAIL")
	)
	private void renderFrozenOverlay(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
		if (minecraft.player != null && minecraft.player.hasEffect(SevenElementsStatusEffects.FROZEN))
			this.renderTextureOverlay(context, POWDER_SNOW_OUTLINE_LOCATION, 1.0F);
	}
}
