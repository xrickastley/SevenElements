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
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
	private void extractTextureOverlay(GuiGraphicsExtractor graphics, Identifier texture, float alpha) {
		throw new AssertionError();
	}

	@Inject(
		method = "extractPlayerHealth",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/Gui;extractArmor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIII)V",
			shift = At.Shift.AFTER
		)
	)
	private void renderAppliedElements(GuiGraphicsExtractor graphics, CallbackInfo ci, @Local Player player, @Local(name = "yLineBase") int yLineBase, @Local(name = "xLeft") int xLeft, @Local(name = "numHealthRows") int numHealthRows, @Local(name = "healthRowHeight") int healthRowHeight) {
		Profiler.get().popPush("seven-elements:elements");

		yLineBase -= (numHealthRows - 1) * healthRowHeight;

		int offset = 1;

		if (player.getArmorValue() > 0) offset++;

		yLineBase -= (10 * (offset));

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
			final int x1 = xLeft + (i * 10);

			graphics.sevenelements$drawCircle(SevenElementsRenderPipelines.CIRCLE, x1 + 4.5f, yLineBase + 4.5f, 4.5f, 0x7F646464);
			graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x1, yLineBase, 0, 0, 9, 9, 9, 9);
		}
	}

	@Inject(
		method = "extractCameraOverlays",
		at = @At("TAIL")
	)
	private void renderFrozenOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (minecraft.player != null && minecraft.player.hasEffect(SevenElementsStatusEffects.FROZEN))
			this.extractTextureOverlay(graphics, POWDER_SNOW_OUTLINE_LOCATION, 1.0F);
	}
}
