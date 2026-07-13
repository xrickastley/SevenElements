package io.github.xrickastley.sevenelements.renderer.genshin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

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

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class ElementRenderer {
	private static final float BLINK_SECONDS = 1.5f;
	private static final float BLINK_COUNT = 3;

	private static final float GAUGE_SCALE = 0.35f;
	private static final float SCALE_PER_GU = 2.5f;

	public static ElementRenderer.ElementState elementState(ElementalApplication application, float tickDelta) {
		return new ElementRenderer.ElementState(
			application.getElement(),
			(application.getRemainingTicks() - tickDelta) / 20.0,
			application.getEntity().level().getGameTime() - application.getAppliedAt(),
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

	public static void renderElement(final SevenElementsLivingEntityRenderState entityState, final ElementState elementState, final SubmitNodeCollector queue, final PoseStack matrixStack, final Camera camera, final float offset) {
		final float blinkInterval = ElementRenderer.BLINK_SECONDS / ElementRenderer.BLINK_COUNT;
		final float intervalSplit = blinkInterval / 2f;

		matrixStack.pushPose();
		matrixStack.translate(0, entityState.sevenelements$getBoundingBoxLength().y() * 1.1, 0);
		matrixStack.mulPose(new Matrix4f().rotation(camera.rotation()));
		matrixStack.scale(0.50F, 0.50F, 0.50F);

		final double blinkProgress = elementState.secondsLeft % blinkInterval;
		final float alpha = (float) (elementState.secondsLeft <= (BLINK_SECONDS + intervalSplit)
			? blinkProgress <= intervalSplit
				? Mth.lerp(blinkProgress / intervalSplit, 0, 1)
				: Mth.lerp(((blinkProgress) - 0.25) / intervalSplit, 1, 0)
			: 1);

		submitElement(elementState.element, queue, matrixStack, offset, alpha);

		if (elementState.appliedForTicks <= 5) {
			final double animationProgress = Ease.LINEAR.applyLerpProgress(elementState.appliedForTicks + elementState.tickDelta, 1, 6);
			final float scale2 = (float) (animationProgress * 2);
			final float alpha2 = (float) (1 - (animationProgress * 0.5));

			matrixStack.scale(scale2, scale2, scale2);

			submitElement(elementState.element, queue, matrixStack, offset, alpha2);
		}

		matrixStack.popPose();
	}

	private static void submitElement(final Element element, final SubmitNodeCollector queue, final PoseStack matrixStack, final float offset, final float alpha) {
		final Identifier texture = element.getTexture();

		if (texture == null) return;

		queue.submitCustomGeometry(
			matrixStack,
			SevenElementsRenderLayers.getElements(texture),
			(entry, consumer) -> {
				final Matrix4f positionMatrix = entry.pose();
				final float finalXOffset = -0.5f + offset;

				consumer.addVertex(positionMatrix, 0 + finalXOffset, 0, 0).setUv(0f, 1f).setColor(1f, 1f, 1f, alpha);
				consumer.addVertex(positionMatrix, 1 + finalXOffset, 0, 0).setUv(1f, 1f).setColor(1f, 1f, 1f, alpha);
				consumer.addVertex(positionMatrix, 1 + finalXOffset, 1, 0).setUv(1f, 0f).setColor(1f, 1f, 1f, alpha);
				consumer.addVertex(positionMatrix, 0 + finalXOffset, 1, 0).setUv(0f, 0f).setColor(1f, 1f, 1f, alpha);
			}
		);
	}

	public static void renderElementalGauge(final SevenElementsLivingEntityRenderState entityState, final ElementGaugeState gaugeState, final SubmitNodeCollector queue, final PoseStack matrixStack, final Camera camera, final float yOffset) {
		if (gaugeState.isEmpty()) return;

		final ClientConfig config = ClientConfig.get();

		matrixStack.pushPose();
		matrixStack.translate(0f, entityState.sevenelements$getBoundingBoxLength().y() * 1.15, 0f);
		matrixStack.mulPose(new Matrix4f().rotation(camera.rotation()));
		matrixStack.scale(GAUGE_SCALE, GAUGE_SCALE * 0.5f, GAUGE_SCALE);

		final float xOffset = (float) (entityState.sevenelements$getBoundingBoxLength().x() * 1.5f) / GAUGE_SCALE;
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

			final Vec3 start = new Vec3(xOffset + i, 0 - yOffset, 3 * 0.001f);
			final Vec3 end = new Vec3(xOffset + i, addedY - yOffset, 3 * 0.001f);

			lineStates.add(new LineState(start, end, 0xff000000, lineWidth));
		}

		submitLineStates(lineStates, queue, SevenElementsRenderLayers.getLines(), matrixStack);

		matrixStack.popPose();
	}

	private static void submitElementalGauge(final SevenElementsLivingEntityRenderState entityState, final ElementGaugeState gaugeState, final SubmitNodeCollector queue, final PoseStack matrixStack, final float gaugeWidth, final float xOffset, final float yOffset) {
		queue.submitCustomGeometry(
			matrixStack,
			SevenElementsRenderLayers.getGaugeDisplay(),
			(entry, consumer) -> {
				final Matrix4f positionMatrix = entry.pose();

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

	private static void submitLineStates(final List<LineState> lineStates, final SubmitNodeCollector queue, final RenderType layer, final PoseStack matrixStack) {
		if (lineStates.isEmpty()) return;

		queue.submitCustomGeometry(
			matrixStack,
			layer,
			(entry, consumer) -> lineStates.forEach(state -> {
				final Vec3 start = state.start;
				final Vec3 end = state.end;
				final Vec3 normal = end.normalize();

				consumer
					.addVertex(entry, start.toVector3f())
					.setColor(0xff000000)
					.setNormal(entry, normal.toVector3f())
					.setLineWidth(state.lineWidth);
				consumer
					.addVertex(entry, end.toVector3f())
					.setColor(0xff000000)
					.setNormal(entry, start.toVector3f())
					.setLineWidth(state.lineWidth);
			})
		);
	}

	private static void drawQuad(VertexConsumer consumer, Matrix4f positionMatrix, float x, float y, float z, float dx, float dy, int color) {
		consumer.addVertex(positionMatrix, x, -y, z * 0.001f).setColor(color);
		consumer.addVertex(positionMatrix, x + dx, -y, z * 0.001f).setColor(color);
		consumer.addVertex(positionMatrix, x + dx, dy - y, z * 0.001f).setColor(color);
		consumer.addVertex(positionMatrix, x, dy - y, z * 0.001f).setColor(color);
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

	private record LineState(Vec3 start, Vec3 end, int color, float lineWidth) {}
}
