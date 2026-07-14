package io.github.xrickastley.sevenelements.renderer.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.DurationElementalApplication;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayers;
import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.Color;

import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.world.phys.Vec3;

public class ElementGaugeFeatureRenderer extends RenderTypeFeatureRenderer<ElementGaugeFeatureRenderer.Submit> {
	public static final float GAUGE_SCALE = 0.35f;
	public static final float SCALE_PER_GU = 2.5f;

	public static final FeatureRendererType<ElementGaugeFeatureRenderer.Submit> TYPE = FeatureRendererType.create("Seven Elements - Elemental Gauge");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<ElementGaugeFeatureRenderer.Submit> submits) {
		for (final ElementGaugeFeatureRenderer.Submit submit : submits)
			this.buildElementalGauge(submit);
	}

	private void buildElementalGauge(ElementGaugeFeatureRenderer.Submit submit) {
		final ClientConfig config = ClientConfig.get();

		final float xOffset = (float) (submit.xOffset * 1.5f) / GAUGE_SCALE;
		final float gaugeWidth = submit.gaugeState.isGaugeUnits()
			? (float) Math.min(SCALE_PER_GU * submit.gaugeState.gaugeUnits, SCALE_PER_GU * 4)
			: 2 * SCALE_PER_GU;

		drawElementalGauge(submit.pose, submit.gaugeState, gaugeWidth, xOffset, submit.yOffset);

		final float scaledGauge = (float) (0.1 * gaugeWidth / submit.gaugeState.gaugeUnits);
		final int splits = (int) Math.floor(gaugeWidth / scaledGauge);

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

			final Vec3 start = new Vec3(xOffset + i, 0 - submit.yOffset, 3 * 0.001f);
			final Vec3 end = new Vec3(xOffset + i, addedY - submit.yOffset, 3 * 0.001f);

			drawLineState(submit.pose, start, end, 0xff000000, lineWidth);
		}
	}

	private void drawElementalGauge(final PoseStack.Pose pose, final ElementGaugeState gaugeState, final float gaugeWidth, final float xOffset, final float yOffset) {
		final VertexConsumer consumer = this.getVertexBuilder(SevenElementsRenderLayers.getGaugeDisplay());

		drawQuad(consumer, pose, xOffset, yOffset, 1, gaugeWidth, 1, 0xffffffff);

		final float progress = (float) (gaugeState.currentGauge / gaugeState.gaugeUnits);
		final Color elementColor = gaugeState.element.getDamageColor();
		final int color = gaugeState.isGaugeUnits()
			? elementColor.asARGB()
			: elementColor.multiply(1, 1, 1, 0.5).asARGB();

		drawQuad(consumer, pose, xOffset, yOffset, 2, gaugeWidth * progress, 1, color);

		if (gaugeState.isDuration()) {
			final float durationProgress = (float) (gaugeState.durationLeft / gaugeState.totalDuration);

			drawQuad(consumer, pose, xOffset, yOffset, 2, gaugeWidth * durationProgress, 1, color);
		}
	}

	private void drawLineState(final PoseStack.Pose entry, final Vec3 start, final Vec3 end, final int color, final float lineWidth) {
		final VertexConsumer consumer = this.getVertexBuilder(SevenElementsRenderLayers.getLines());

		final Vec3 normal = end.normalize();

		consumer
			.addVertex(entry, start.toVector3f())
			.setColor(0xff000000)
			.setNormal(entry, normal.toVector3f())
			.setLineWidth(lineWidth);
		consumer
			.addVertex(entry, end.toVector3f())
			.setColor(0xff000000)
			.setNormal(entry, start.toVector3f())
			.setLineWidth(lineWidth);
	}

	private static void drawQuad(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float dx, float dy, int color) {
		consumer.addVertex(pose, x, -y, z * 0.001f).setColor(color);
		consumer.addVertex(pose, x + dx, -y, z * 0.001f).setColor(color);
		consumer.addVertex(pose, x + dx, dy - y, z * 0.001f).setColor(color);
		consumer.addVertex(pose, x, dy - y, z * 0.001f).setColor(color);
	}

	public record Submit(PoseStack.Pose pose, ElementGaugeState gaugeState, float xOffset, float yOffset) implements SubmitNode {
		@Override
		public FeatureRendererType<ElementGaugeFeatureRenderer.Submit> featureType() {
			return ElementGaugeFeatureRenderer.TYPE;
		}
	}

	public record ElementGaugeState(Element element, ElementalApplication.Type type, double gaugeUnits, double currentGauge, @Nullable Double durationLeft, @Nullable Double totalDuration) {
		public ElementGaugeState(ElementalApplication application, float tickDelta) {
			this(
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
}
