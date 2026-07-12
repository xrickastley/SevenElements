package io.github.xrickastley.sevenelements.renderer.genshin;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import io.github.xrickastley.sevenelements.element.DurationElementalApplication;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.interfaces.SevenElementsLivingEntityRenderState;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayers;
import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.Color;
import io.github.xrickastley.sevenelements.util.Ease;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class ElementRenderer {
	private static final float BLINK_SECONDS = 1.5f;
	private static final float BLINK_COUNT = 3;

	private static final float GAUGE_SCALE = 0.35f;
	private static final float SCALE_PER_GU = 2.5f;

	public static ElementRenderer.ElementState elementState(ElementalApplication application, float tickDelta) {
		return new ElementRenderer.ElementState(
			application.getElement(),
			(application.getRemainingTicks() - tickDelta) / 20.0,
			application.getEntity().getEntityWorld().getTime() - application.getAppliedAt(),
			tickDelta
		);
	}

	public static ElementRenderer.ElementGaugeState gaugeState(ElementalApplication application, float tickDelta) {
		return new ElementRenderer.ElementGaugeState(
			application.getElement(),
			application.getType(),
			application.getGaugeUnits(),
			application.getCurrentGauge(),
			application instanceof final DurationElementalApplication durationApp
				? (double) (durationApp.getRemainingTicks() - tickDelta)
				: null,
			application instanceof final DurationElementalApplication durationApp
				? durationApp.getDuration()
				: null
		);
	}

	public static void renderElement(final SevenElementsLivingEntityRenderState entityState, final ElementState elementState, final OrderedRenderCommandQueue queue, final MatrixStack matrixStack, final Camera camera, final float offset) {
		final float blinkInterval = ElementRenderer.BLINK_SECONDS / ElementRenderer.BLINK_COUNT;
		final float intervalSplit = blinkInterval / 2f;

		matrixStack.push();
		matrixStack.translate(0, entityState.sevenelements$getBoundingBoxLength().getY() * 1.1, 0);
		matrixStack.multiplyPositionMatrix(new Matrix4f().rotation(camera.getRotation()));
		matrixStack.scale(0.50F, 0.50F, 0.50F);

		final double blinkProgress = elementState.secondsLeft % blinkInterval;
		final float alpha = (float) (elementState.secondsLeft <= (BLINK_SECONDS + intervalSplit)
			? blinkProgress <= intervalSplit
				? MathHelper.lerp(blinkProgress / intervalSplit, 0, 1)
				: MathHelper.lerp(((blinkProgress) - 0.25) / intervalSplit, 1, 0)
			: 1);

		submitElement(elementState.element, queue, matrixStack, offset, alpha);

		if (elementState.appliedForTicks <= 5) {
			final double animationProgress = Ease.LINEAR.applyLerpProgress(elementState.appliedForTicks + elementState.tickDelta, 1, 6);
			final float scale2 = (float) (animationProgress * 2);
			final float alpha2 = (float) (1 - (animationProgress * 0.5));

			matrixStack.scale(scale2, scale2, scale2);

			submitElement(elementState.element, queue, matrixStack, offset, alpha2);
		}

		matrixStack.pop();
	}

	private static void submitElement(final Element element, final OrderedRenderCommandQueue queue, final MatrixStack matrixStack, final float offset, final float alpha) {
		final Identifier texture = element.getTexture();

		if (texture == null) return;

		queue.submitCustom(
			matrixStack,
			SevenElementsRenderLayers.getElements(texture),
			(entry, consumer) -> {
				final Matrix4f positionMatrix = entry.getPositionMatrix();
				final float finalXOffset = -0.5f + offset;

				consumer.vertex(positionMatrix, 0 + finalXOffset, 0, 0).texture(0f, 1f).color(1f, 1f, 1f, alpha);
				consumer.vertex(positionMatrix, 1 + finalXOffset, 0, 0).texture(1f, 1f).color(1f, 1f, 1f, alpha);
				consumer.vertex(positionMatrix, 1 + finalXOffset, 1, 0).texture(1f, 0f).color(1f, 1f, 1f, alpha);
				consumer.vertex(positionMatrix, 0 + finalXOffset, 1, 0).texture(0f, 0f).color(1f, 1f, 1f, alpha);
			}
		);
	}

	public static void renderElementalGauge(final SevenElementsLivingEntityRenderState entityState, final ElementGaugeState gaugeState, final OrderedRenderCommandQueue queue, final MatrixStack matrixStack, final Camera camera, final float yOffset) {
		if (gaugeState.isEmpty()) return;

		final ClientConfig config = ClientConfig.get();

		matrixStack.push();
		matrixStack.translate(0f, entityState.sevenelements$getBoundingBoxLength().getY() * 1.15, 0f);
		matrixStack.multiplyPositionMatrix(new Matrix4f().rotation(camera.getRotation()));
		matrixStack.scale(GAUGE_SCALE, GAUGE_SCALE * 0.5f, GAUGE_SCALE);

		final float xOffset = (float) (entityState.sevenelements$getBoundingBoxLength().getX() * 1.5f) / GAUGE_SCALE;
		final float gaugeWidth = gaugeState.isGaugeUnits()
			? (float) Math.min(SCALE_PER_GU * gaugeState.gaugeUnits, SCALE_PER_GU * 4)
			: 2 * SCALE_PER_GU;

		submitElementalGauge(entityState, gaugeState, queue, matrixStack, gaugeWidth, xOffset, yOffset);

		final float scaledGauge = (float) (0.1 * gaugeWidth / gaugeState.gaugeUnits);
		final int splits = (int) Math.floor(gaugeWidth / scaledGauge);

		final List<LineState> lineStates = new ArrayList<>();

		for (int c = 1; c < splits && config.developer.displayGaugeRuler; c++) {
			final float i = c * scaledGauge;

			final float addedY = c % 10 == 0
				? 1f
				: c % 5 == 0
					? 0.5f
					: 0.25f;

			final int lineWidth = c % 10 == 0
				? 10
				: 5;

			final Vec3d start = new Vec3d(xOffset + i, 0 - yOffset, 3 * 0.001f);
			final Vec3d end = new Vec3d(xOffset + i, addedY - yOffset, 3 * 0.001f);

			lineStates.add(new LineState(start, end, 0xff000000, lineWidth));
		}

		submitLineStates(lineStates, queue, SevenElementsRenderLayers.getLines(), matrixStack);

		matrixStack.pop();
	}

	private static void submitElementalGauge(final SevenElementsLivingEntityRenderState entityState, final ElementGaugeState gaugeState, final OrderedRenderCommandQueue queue, final MatrixStack matrixStack, final float gaugeWidth, final float xOffset, final float yOffset) {
		queue.submitCustom(
			matrixStack,
			SevenElementsRenderLayers.getGaugeDisplay(),
			(entry, consumer) -> {
				final Matrix4f positionMatrix = entry.getPositionMatrix();

				drawQuad(consumer, positionMatrix, xOffset, yOffset, 1, gaugeWidth, 1, 0xffffffff);

				final float progress = (float) (gaugeState.currentGauge / gaugeState.gaugeUnits);
				final Color elementColor = gaugeState.element.getDamageColor();
				final int color = gaugeState.isGaugeUnits()
					? elementColor.asARGB()
					: elementColor.multiply(1, 1, 1, 0.5).asARGB();

				drawQuad(consumer, positionMatrix, xOffset, yOffset, 2, gaugeWidth * progress, 1, color);

				if (gaugeState.isDuration()) {
					final float durationProgress = (float) (gaugeState.durationLeft / gaugeState.totalDuration);

					drawQuad(consumer, positionMatrix, xOffset, yOffset, 2, gaugeWidth * durationProgress, 1, color);
				}
			}
		);
	}

	private static void submitLineStates(final List<LineState> lineStates, final OrderedRenderCommandQueue queue, final RenderLayer layer, final MatrixStack matrixStack) {
		if (lineStates.isEmpty()) return;

		queue.submitCustom(
			matrixStack,
			layer,
			(entry, consumer) -> lineStates.forEach(state -> {
				final Vec3d start = state.start;
				final Vec3d end = state.end;
				final Vec3d normal = end.normalize();

				consumer
					.vertex(entry, start.toVector3f())
					.color(0xff000000)
					.normal(entry, normal.toVector3f())
					.lineWidth(state.lineWidth);
				consumer
					.vertex(entry, end.toVector3f())
					.color(0xff000000)
					.normal(entry, start.toVector3f())
					.lineWidth(state.lineWidth);
			})
		);
	}

	private static void drawQuad(VertexConsumer consumer, Matrix4f positionMatrix, float x, float y, float z, float dx, float dy, int color) {
		consumer.vertex(positionMatrix, x, -y, z * 0.001f).color(color);
		consumer.vertex(positionMatrix, x + dx, -y, z * 0.001f).color(color);
		consumer.vertex(positionMatrix, x + dx, dy - y, z * 0.001f).color(color);
		consumer.vertex(positionMatrix, x, dy - y, z * 0.001f).color(color);
	}



	public record ElementState(Element element, double secondsLeft, long appliedForTicks, float tickDelta) {}

	public record ElementGaugeState(Element element, ElementalApplication.Type type, double gaugeUnits, double currentGauge, @Nullable Double durationLeft, @Nullable Double totalDuration) {
		public boolean isEmpty() {
			return this.currentGauge <= 0
				|| (this.durationLeft != null && this.durationLeft <= 0);
		}

		public boolean isGaugeUnits() {
			return this.type == ElementalApplication.Type.GAUGE_UNIT;
		}

		public boolean isDuration() {
			return this.type == ElementalApplication.Type.DURATION;
		}
	}

	private record LineState(Vec3d start, Vec3d end, int color, float lineWidth) {}
}
