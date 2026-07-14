package io.github.xrickastley.sevenelements.renderer.feature;

import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

import org.joml.Matrix4fc;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayers;

import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;

public class ElementFeatureRenderer extends RenderTypeFeatureRenderer<ElementFeatureRenderer.Submit> {
	public static final float BLINK_SECONDS = 1.5f;
	public static final float BLINK_COUNT = 3;

	public static final FeatureRendererType<ElementFeatureRenderer.Submit> TYPE = FeatureRendererType.create("Seven Elements - Element");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<ElementFeatureRenderer.Submit> submits) {
		for (final ElementFeatureRenderer.Submit submit : submits) {
			final VertexConsumer consumer = this.getVertexBuilder(SevenElementsRenderLayers.getElements(submit.element.getTexture()));
			final float xOffset = -0.5f + submit.offset;

			consumer.addVertex(submit.pose, 0 + xOffset, 0, 0).setUv(0f, 1f).setColor(1f, 1f, 1f, submit.alpha);
			consumer.addVertex(submit.pose, 1 + xOffset, 0, 0).setUv(1f, 1f).setColor(1f, 1f, 1f, submit.alpha);
			consumer.addVertex(submit.pose, 1 + xOffset, 1, 0).setUv(1f, 0f).setColor(1f, 1f, 1f, submit.alpha);
			consumer.addVertex(submit.pose, 0 + xOffset, 1, 0).setUv(0f, 0f).setColor(1f, 1f, 1f, submit.alpha);
		}
	}

	public record Submit(Matrix4fc pose, Element element, float offset, float alpha) implements SubmitNode {
		@Override
		public FeatureRendererType<ElementFeatureRenderer.Submit> featureType() {
			return ElementFeatureRenderer.TYPE;
		}
	}

	public record ElementState(Element element, double secondsLeft, long appliedForTicks, float tickDelta) {
		public ElementState(ElementalApplication application, float tickDelta) {
			this(
				application.getElement(),
				(application.getRemainingTicks() - tickDelta) / 20.0,
				application.getEntity().level().getGameTime() - application.getAppliedAt(),
				tickDelta
			);
		}
	}
}
