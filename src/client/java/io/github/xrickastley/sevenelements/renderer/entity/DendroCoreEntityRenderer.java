package io.github.xrickastley.sevenelements.renderer.entity;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.renderer.entity.model.DendroCoreEntityModel;
import io.github.xrickastley.sevenelements.util.Ease;
import io.github.xrickastley.sevenelements.util.MathHelper2;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DendroCoreEntityRenderer extends LivingEntityRenderer<DendroCoreEntity, EntityModel<DendroCoreEntity>> {
	public DendroCoreEntityRenderer(EntityRendererFactory.Context context) {
		super(
			context,
			DendroCoreEntityRenderer.createModel(context),
			0.5f
		);
	}

	private static EntityModel<DendroCoreEntity> createModel(EntityRendererFactory.Context context) {
		return new DendroCoreEntityModel(context.getPart(DendroCoreEntityModel.MODEL_LAYER));
	}

	public Identifier getTexture(DendroCoreEntity entity) {
		return SevenElements.identifier("textures/entity/dendro_core/dendro_core.png");
	}

	@Override
	public void render(DendroCoreEntity dendroCore, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
		this.shadowOpacity = 0f;
		this.shadowRadius = 0f;

		super.render(dendroCore, f, g, matrixStack, vertexConsumerProvider, i);
	}

	@Override
	protected void scale(DendroCoreEntity dendroCore, MatrixStack matrixStack, float delta) {
		final double explodeProgress = Ease.IN_QUAD.applyLerp(MathHelper2.endOffset(dendroCore.age + delta, 2, 0, 120), 0, 1.5);
		final float scale = !dendroCore.isHyperbloom()
			? 0.5f + (float) (explodeProgress * 5)
			: 0.35f;

		matrixStack.translate(0, 0, 0);
		matrixStack.scale(scale, scale, scale);
	}

	@Override
	protected void renderLabelIfPresent(DendroCoreEntity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {}
}
