package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.Color;
import io.github.xrickastley.sevenelements.util.Ease;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public final class WorldTextRenderer {
	private final List<Entry> entries = new ArrayList<>();

	public void render(LevelRenderContext context) {
		final Camera camera = context.gameRenderer().getMainCamera();
		final PoseStack matrixStack = new PoseStack();

		entries.forEach(entry -> entry.render(camera, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false), matrixStack));
	}

	public void tick(ClientLevel world) {
		entries.forEach(Entry::tick);
		entries.removeIf(Entry::shouldRemove);
	}

	public WorldTextRenderer addEntry(Entry entry) {
		this.entries.add(entry);

		return this;
	}

	public static void drawText(final Camera camera, final PoseStack matrices, final MultiBufferSource vertexConsumers, final FormattedCharSequence text, final double x, final double y, final double z, final int color, final float size, final boolean center, final float offset, final boolean visibleThroughObjects) {
		final Minecraft client = Minecraft.getInstance();
		final Font textRenderer = client.font;
		final ClientConfig config = ClientConfig.get();

		final double d = camera.position().x;
		final double e = camera.position().y;
		final double f = camera.position().z;

		final float scale = (float) (size * config.rendering.text.globalTextScale);

		matrices.pushPose();
		matrices.translate((float) (x - d), (float) (y - e), (float) (z - f));
		matrices.mulPose(new Matrix4f().rotation(camera.rotation()));
		matrices.scale(scale, -scale, scale);

		float g = center ? (-textRenderer.width(text) / 2.0f) : 0.0f;
		g -= offset / size;

		textRenderer.drawInBatch(text, g, 0.0f, color, false, matrices.last().pose(), vertexConsumers, visibleThroughObjects ? DisplayMode.SEE_THROUGH : DisplayMode.NORMAL, 0, 15728880);

		matrices.popPose();
	}

	public static abstract class Entry {
		protected final double x;
		protected final double y;
		protected final double z;
		protected final Color color;
		protected int age;

		Entry(double x, double y, double z, Color color) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.color = color;
			this.age = 0;
		}

		protected abstract void render(Camera camera, float tickDelta, PoseStack matrices);

		protected void tick() {
			this.age++;
		}

		protected abstract boolean shouldRemove();
	}

	public static final class ReactionText extends Entry {
		protected final Component text;
		protected final int maxAge = 30;
		protected final int fadeAge = maxAge - 15;
		protected final int scaleAge = 8;

		public ReactionText(double x, double y, double z, Color color, Component text) {
			super(x, y, z, color);

			this.text = text;
		}

		@Override
		protected void render(Camera camera, float tickDelta, PoseStack matrices) {
			final Minecraft client = Minecraft.getInstance();
			final MultiBufferSource.BufferSource immediate = client.renderBuffers().bufferSource();

			final float deltaTime = age + tickDelta;

			final double alpha = Math.max(0.0f, Mth.lerp((deltaTime - fadeAge) / (maxAge - fadeAge), 1.0, 0.0));
			final double scale = 1.25 - (Ease.IN_OUT_QUART.applyLerpProgress(deltaTime / scaleAge, 0, 1) * 0.5);

			if (alpha <= 0f || scale <= 0f) return;

			final double x = this.x;
			final double y = this.y + Ease.OUT_SINE.applyLerpProgress(deltaTime, 0, maxAge) * 0.75f;
			final double z = this.z;

			final int color = this.color
				.multiply(1, 1, 1, alpha)
				.asARGB();

			WorldTextRenderer.drawText(camera, matrices, immediate, this.text.getVisualOrderText(), x, y, z, color, 0.04f * (float) scale, true, 0f, true);

			immediate.endBatch();
		}

		@Override
		protected boolean shouldRemove() {
			return age > maxAge;
		}
	}

	public static final class DamageText extends Entry {
		protected final int maxAge = 30;
		protected final int fadeAge = maxAge - 15;
		protected final int scaleAge = 12;
		protected final Component amount;
		protected final double scale;

		public DamageText(double x, double y, double z, Color color, double amount, double scale) {
			super(x, y, z, color);

			final ClientConfig config = ClientConfig.get();
			final String damageFormat = config.developer.commafyDamage
				? "%,.0f"
				: "%.0f";

			this.amount = TextHelper.font(String.format(damageFormat, Math.max(amount, 1)), TextHelper.GENSHIN_FONT);
			this.scale = scale;
		}

		@Override
		protected void render(Camera camera, float tickDelta, PoseStack matrices) {
			final MultiBufferSource.BufferSource immediate = SevenElementsRenderLayer.getWorldTextImmediate();

			final float deltaTime = age + tickDelta;

			final double alpha = Math.max(0.0f, Mth.lerp((deltaTime - fadeAge) / (maxAge - fadeAge), 1.0, 0.0));
			final double scale = (1.25 - (Ease.IN_OUT_QUART.applyLerpProgress(deltaTime / scaleAge, 0, 1) * 0.5)) * this.scale;

			if (alpha <= 0f || scale <= 0f) return;

			final double x = this.x;
			final double y = this.y + Ease.OUT_SINE.applyLerpProgress(deltaTime, 0, maxAge) * 0.75f;
			final double z = this.z;

			final int color = this.color
				.multiply(1, 1, 1, alpha)
				.asARGB();

			WorldTextRenderer.drawText(camera, matrices, immediate, this.amount.getVisualOrderText(), x, y, z, color, 0.04f * (float) scale, true, 0f, true);

			immediate.endBatch();
		}

		@Override
		protected boolean shouldRemove() {
			return age > maxAge;
		}
	}
}
