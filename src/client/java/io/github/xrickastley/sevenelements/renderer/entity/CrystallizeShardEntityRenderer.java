package io.github.xrickastley.sevenelements.renderer.entity;

import java.util.Set;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.entity.CrystallizeShardEntity;
import io.github.xrickastley.sevenelements.renderer.entity.model.CrystallizeShardEntityModel;
import io.github.xrickastley.sevenelements.renderer.entity.state.CrystallizeShardEntityState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CrystallizeShardEntityRenderer extends LivingEntityRenderer<CrystallizeShardEntity, CrystallizeShardEntityState, CrystallizeShardEntityModel> {
	private static final Set<Element> VALID_ELEMENTS = Set.of(Element.PYRO, Element.HYDRO, Element.ELECTRO, Element.CRYO, Element.GEO);

	public CrystallizeShardEntityRenderer(EntityRendererFactory.Context context) {
		super(
			context,
			CrystallizeShardEntityRenderer.createModel(context),
			0.5f
		);
	}

	private static CrystallizeShardEntityModel createModel(EntityRendererFactory.Context context) {
		return new CrystallizeShardEntityModel(context.getPart(CrystallizeShardEntityModel.MODEL_LAYER));
	}

	@Override
	public Identifier getTexture(CrystallizeShardEntityState state) {
		return state.element != null && VALID_ELEMENTS.contains(state.element)
			? SevenElements.identifier("textures/entity/crystallize_shard/crystallize_shard_" + state.element.toString().toLowerCase() + ".png")
			: SevenElements.identifier("textures/entity/crystallize_shard/crystallize_shard.png");
	}

	@Override
	public CrystallizeShardEntityState createRenderState() {
		return new CrystallizeShardEntityState();
	}

	@Override
	public void updateRenderState(CrystallizeShardEntity crystallizeShard, CrystallizeShardEntityState state, float f) {
		super.updateRenderState(crystallizeShard, state, f);

		state.element = crystallizeShard.getElement();
		state.idleAnimationState = crystallizeShard.idleAnimationState;
	}

	@Override
	public void render(CrystallizeShardEntityState state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
		if (state.element == null) return;

		this.shadowOpacity = 0f;
		this.shadowRadius = 0f;

		super.render(state, matrixStack, vertexConsumerProvider, i);
	}

	@Override
	protected void renderLabelIfPresent(CrystallizeShardEntityState state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {}
}
