package io.github.xrickastley.sevenelements.renderer;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.Color;
import io.github.xrickastley.sevenelements.util.Ease;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public final class WorldTextRenderer {
	private final List<Entry> entries = new ArrayList<>();

	public void render(WorldRenderContext context) {
		final Camera camera = context.camera();
		final MatrixStack matrixStack = new MatrixStack();

		matrixStack.push();

		// Implement legacy renderWorld transforms.
		matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

		entries.forEach(entry -> entry.render(camera, context.tickCounter().getTickProgress(false), matrixStack));

		matrixStack.pop();
	}

	public void tick(ClientWorld world) {
		entries.forEach(Entry::tick);
		entries.removeIf(Entry::shouldRemove);
	}

	public WorldTextRenderer addEntry(Entry entry) {
		this.entries.add(entry);

		return this;
	}

	public static void drawText(final Camera camera, final MatrixStack matrices, final VertexConsumerProvider vertexConsumers, final OrderedText text, final double x, final double y, final double z, final int color, final float size, final boolean center, final float offset, final boolean visibleThroughObjects) {
		final MinecraftClient client = MinecraftClient.getInstance();
		final TextRenderer textRenderer = client.textRenderer;
		final ClientConfig config = ClientConfig.get();

		final double d = camera.getPos().x;
		final double e = camera.getPos().y;
		final double f = camera.getPos().z;

		final float scale = (float) (size * config.rendering.text.globalTextScale);

		matrices.push();
		matrices.translate((float) (x - d), (float) (y - e), (float) (z - f));
		matrices.multiplyPositionMatrix(new Matrix4f().rotation(camera.getRotation()));
		matrices.scale(scale, -scale, scale);

		float g = center ? (-textRenderer.getWidth(text) / 2.0f) : 0.0f;
		g -= offset / size;

		textRenderer.draw(text, g, 0.0f, color, false, matrices.peek().getPositionMatrix(), vertexConsumers, visibleThroughObjects ? TextLayerType.SEE_THROUGH : TextLayerType.NORMAL, 0, 15728880);

		matrices.pop();
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

		protected abstract void render(Camera camera, float tickDelta, MatrixStack matrices);

		protected void tick() {
			this.age++;
		}

		protected abstract boolean shouldRemove();
	}

	public static final class ReactionText extends Entry {
		protected final Text text;
		protected final int maxAge = 30;
		protected final int fadeAge = maxAge - 15;
		protected final int scaleAge = 8;

		public ReactionText(double x, double y, double z, Color color, Text text) {
			super(x, y, z, color);

			this.text = text;
		}

		@Override
		protected void render(Camera camera, float tickDelta, MatrixStack matrices) {
			final MinecraftClient client = MinecraftClient.getInstance();
			final VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

			final float deltaTime = age + tickDelta;

			final double alpha = Math.max(0.0f, MathHelper.lerp((deltaTime - fadeAge) / (maxAge - fadeAge), 1.0, 0.0));
			final double scale = 1.25 - (Ease.IN_OUT_QUART.applyLerpProgress(deltaTime / scaleAge, 0, 1) * 0.5);

			if (alpha <= 0f || scale <= 0f) return;

			final double x = this.x;
			final double y = this.y + Ease.OUT_SINE.applyLerpProgress(deltaTime, 0, maxAge) * 0.75f;
			final double z = this.z;

			final int color = this.color
				.multiply(1, 1, 1, alpha)
				.asARGB();

			WorldTextRenderer.drawText(camera, matrices, immediate, this.text.asOrderedText(), x, y, z, color, 0.04f * (float) scale, true, 0f, true);

			immediate.draw();
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
		protected final Text amount;
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
		protected void render(Camera camera, float tickDelta, MatrixStack matrices) {
			// final MinecraftClient client = MinecraftClient.getInstance();
			// final VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
			final VertexConsumerProvider.Immediate immediate = SevenElementsRenderLayer.getWorldTextImmediate();

			final float deltaTime = age + tickDelta;

			final double alpha = Math.max(0.0f, MathHelper.lerp((deltaTime - fadeAge) / (maxAge - fadeAge), 1.0, 0.0));
			final double scale = (1.25 - (Ease.IN_OUT_QUART.applyLerpProgress(deltaTime / scaleAge, 0, 1) * 0.5)) * this.scale;

			if (alpha <= 0f || scale <= 0f) return;

			final double x = this.x;
			final double y = this.y + Ease.OUT_SINE.applyLerpProgress(deltaTime, 0, maxAge) * 0.75f;
			final double z = this.z;

			final int color = this.color
				.multiply(1, 1, 1, alpha)
				.asARGB();

			WorldTextRenderer.drawText(camera, matrices, immediate, this.amount.asOrderedText(), x, y, z, color, 0.04f * (float) scale, true, 0f, true);

			immediate.draw();
		}

		@Override
		protected boolean shouldRemove() {
			return age > maxAge;
		}
	}
}
