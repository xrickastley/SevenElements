package io.github.xrickastley.sevenelements.renderer.entity;

import java.util.Set;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.entity.CrystallizeShardEntity;
import io.github.xrickastley.sevenelements.renderer.entity.model.CrystallizeShardEntityModel;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CrystallizeShardEntityRenderer extends LivingEntityRenderer<CrystallizeShardEntity, EntityModel<CrystallizeShardEntity>> {
	private static final Set<Element> VALID_ELEMENTS = Set.of(Element.PYRO, Element.HYDRO, Element.ELECTRO, Element.CRYO, Element.GEO);

	public CrystallizeShardEntityRenderer(EntityRendererFactory.Context context) {
		super(
			context,
			CrystallizeShardEntityRenderer.createModel(context),
			0.5f
		);
	}

	private static EntityModel<CrystallizeShardEntity> createModel(EntityRendererFactory.Context context) {
		return new CrystallizeShardEntityModel(context.getPart(CrystallizeShardEntityModel.MODEL_LAYER));
	}

	public Identifier getTexture(CrystallizeShardEntity entity) {
		return entity.getElement() != null && VALID_ELEMENTS.contains(entity.getElement())
			? SevenElements.identifier("textures/entity/crystallize_shard/crystallize_shard_" + entity.getElement().toString().toLowerCase() + ".png")
			: SevenElements.identifier("textures/entity/crystallize_shard/crystallize_shard.png");
	}

	@Override
	public void render(CrystallizeShardEntity crystallizeShard, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
		if (crystallizeShard.getElement() == null) return;

		this.shadowOpacity = 0f;
		this.shadowRadius = 0f;

		super.render(crystallizeShard, f, g, matrixStack, vertexConsumerProvider, i);
	}

	@Override
	protected void scale(CrystallizeShardEntity crystallizeShard, MatrixStack matrixStack, float delta) {
		final float scale = 1f;
		matrixStack.translate(0, 0, 0);
		matrixStack.scale(scale, scale, scale);
	}

	@Override
	protected void renderLabelIfPresent(CrystallizeShardEntity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {}
}
