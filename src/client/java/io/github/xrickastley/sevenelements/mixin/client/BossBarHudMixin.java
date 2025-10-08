package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.util.Array;
import io.github.xrickastley.sevenelements.util.CircleRenderer;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.Identifier;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@ModifyConstant(
		method = "render",
		constant = @Constant(intValue = 9, ordinal = 1)
	)
	private int addElementsToRender(int value, @Local ClientBossBar bossBar) {
		if (bossBar.sevenelements$getEntity() == null) return value;

		final int shift = ElementComponent.KEY
			.get(bossBar.sevenelements$getEntity())
			.getAppliedElements()
			.isEmpty() ? 0 : 8;

		return value + shift;
	}

	@Inject(
		method = "renderBossBar(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/entity/boss/BossBar;)V",
		at = @At("TAIL")
	)
	private void renderAppliedElements(DrawContext context, int x, int y, BossBar bossBar, CallbackInfo ci) {
		if (bossBar.sevenelements$getEntity() == null || bossBar.sevenelements$getEntity().isDead()) return;

		final double RADIUS = 5;
		final int BOUND = (int) (RADIUS * 2);
		final int SHIFT = 1;
		final int INNER_BOUND = (int) ((RADIUS - SHIFT) * 2);

		this.client.getProfiler().swap("seven-elements:elements");

		y += 6;

		final double scaleFactor = MinecraftClient.getInstance().getWindow().getScaleFactor();
		final Set<Identifier> existing = new HashSet<>();
		final Array<Identifier> appliedElements = ElementComponent.KEY
			.get(bossBar.sevenelements$getEntity())
			.getAppliedElements()
			.map(Functions.compose(ElementalApplication::getElement, Element::getTexture))
			.filter(existing::add);

		for (int i = 0; i < appliedElements.length(); i++) {
			final Identifier texture = appliedElements.get(i);
			final int x1 = x + (i * (BOUND + 1));
			final CircleRenderer circleRenderer = new CircleRenderer((x1 + RADIUS) * scaleFactor, (y + RADIUS) * scaleFactor, 0);

			circleRenderer
				.add(RADIUS * scaleFactor, 1, 0x7F646464)
				.draw(context.getMatrices().peek().getPositionMatrix());

			context.drawTexture(texture, x1 + SHIFT, y + SHIFT, INNER_BOUND, INNER_BOUND, 0, 0, INNER_BOUND, INNER_BOUND, INNER_BOUND, INNER_BOUND);
		}
	}
}
