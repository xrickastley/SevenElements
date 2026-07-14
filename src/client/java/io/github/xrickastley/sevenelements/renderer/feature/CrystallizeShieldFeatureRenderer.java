package io.github.xrickastley.sevenelements.renderer.feature;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayers;
import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.SphereRenderer;

import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;

public class CrystallizeShieldFeatureRenderer extends RenderTypeFeatureRenderer<CrystallizeShieldFeatureRenderer.Submit> {
	public static final FeatureRendererType<CrystallizeShieldFeatureRenderer.Submit> TYPE = FeatureRendererType.create("Seven Elements - Crystallize Shield");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		final ClientConfig config = ClientConfig.get();

		for (final CrystallizeShieldFeatureRenderer.Submit submit : submits) {
			SphereRenderer.render(
				this.getVertexBuilder(SevenElementsRenderLayers.getCrystallizeShield()),
				submit.pose,
				(float) (submit.yLength / 2 * 1.25),
				config.rendering.elements.sphereResolution,
				config.rendering.elements.sphereResolution * 2,
				pos -> submit.element.getDamageColor().multiply(1, 1, 1, 0.75 * Math.pow(pos.x, 4)).asARGB()
			);
		}
	}

	public record Submit(PoseStack.Pose pose, Element element, float yLength) implements SubmitNode {
		@Override
		public FeatureRendererType<CrystallizeShieldFeatureRenderer.Submit> featureType() {
			return CrystallizeShieldFeatureRenderer.TYPE;
		}
	}
}
