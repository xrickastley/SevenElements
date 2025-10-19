package io.github.xrickastley.sevenelements.mixin.client;

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
import io.github.xrickastley.sevenelements.util.Array;
import io.github.xrickastley.sevenelements.util.CircleRenderer;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	@Final
	private static Identifier POWDER_SNOW_OUTLINE;

	@Shadow
	private int renderHealthValue;
	@Shadow
	private int scaledWidth;
	@Shadow
	private int scaledHeight;

	@Shadow
	private PlayerEntity getCameraPlayer() {
		throw new AssertionError();
	}

	@Shadow
	private void renderOverlay(DrawContext context, Identifier texture, float opacity) {
		throw new AssertionError();
	}

	@Inject(
		method = "renderStatusBars",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
			shift = At.Shift.BEFORE,
			ordinal = 0
		)
	)
	private void renderAppliedElements(DrawContext context, CallbackInfo ci) {
		final PlayerEntity player = this.getCameraPlayer();

		if (player == null) return;

		// Moved computations for Connector compat.
		int _i = MathHelper.ceil(player.getHealth());
		int j = this.renderHealthValue;

		final int x = this.scaledWidth / 2 - 91;

		int o = this.scaledHeight - 39;
		float f = Math.max((float) player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH), Math.max(j, _i));
		int p = MathHelper.ceil(player.getAbsorptionAmount());
		int q = MathHelper.ceil((f + p) / 2.0F / 10.0F);
		int r = Math.max(10 - (q - 2), 3);
		int y = o - (q - 1) * r - 10;

		this.client.getProfiler().swap("origins-genshin:elements");

		int offset = 0;

		if (player.getArmor() > 0) offset += 1;

		y -= (10 * (offset));

		final ElementComponent component = ElementComponent.KEY.get(player);

		final double scaleFactor = MinecraftClient.getInstance().getWindow().getScaleFactor();
		final Set<Identifier> existing = new HashSet<>();
		final Array<Identifier> appliedElements = component
			.getAppliedElements()
			.map(Functions.compose(ElementalApplication::getElement, Element::getTexture))
			.filter(existing::add);

		if (component.getCrystallizeShield() != null && component.getCrystallizeShield().getRight() > 0)
			appliedElements.add(SevenElements.identifier("textures/status_effect/defense.png"));

		for (int i = 0; i < appliedElements.length(); i++) {
			final Identifier texture = appliedElements.get(i);
			final int x1 = x + (i * 10);
			final CircleRenderer circleRenderer = new CircleRenderer((x1 + 4.5) * scaleFactor, (y + 4.5) * scaleFactor, 0);

			circleRenderer
				.add(4.5 * scaleFactor, 1, 0x7F646464)
				.draw(context.getMatrices().peek().getPositionMatrix());

			context.drawTexture(texture, x1, y, 9, 9, 0, 0, 9, 9, 9, 9);
		}
	}

	@Inject(
		method = "render(Lnet/minecraft/client/gui/DrawContext;F)V",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;getTextRenderer()Lnet/minecraft/client/font/TextRenderer;",
			shift = At.Shift.AFTER
		)
	)
	private void renderFrozenOverlay(DrawContext context, float tickDelta, CallbackInfo ci) {
		if (client.player != null && client.player.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			this.renderOverlay(context, POWDER_SNOW_OUTLINE, 1.0F);
	}
}
