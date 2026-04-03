package io.github.xrickastley.sevenelements.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Set;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.entity.CrystallizeShardEntity;
import io.github.xrickastley.sevenelements.renderer.entity.model.CrystallizeShardEntityModel;
import io.github.xrickastley.sevenelements.renderer.entity.state.CrystallizeShardEntityState;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public class CrystallizeShardEntityRenderer extends LivingEntityRenderer<CrystallizeShardEntity, CrystallizeShardEntityState, CrystallizeShardEntityModel> {
	private static final Set<Element> VALID_ELEMENTS = Set.of(Element.PYRO, Element.HYDRO, Element.ELECTRO, Element.CRYO, Element.GEO);

	public CrystallizeShardEntityRenderer(EntityRendererProvider.Context context) {
		super(
			context,
			CrystallizeShardEntityRenderer.createModel(context),
			0.5f
		);
	}

	private static CrystallizeShardEntityModel createModel(EntityRendererProvider.Context context) {
		return new CrystallizeShardEntityModel(context.bakeLayer(CrystallizeShardEntityModel.MODEL_LAYER));
	}

	@Override
	public Identifier getTextureLocation(CrystallizeShardEntityState state) {
		return state.element != null && VALID_ELEMENTS.contains(state.element)
			? SevenElements.identifier("textures/entity/crystallize_shard/crystallize_shard_" + state.element.toString().toLowerCase() + ".png")
			: SevenElements.identifier("textures/entity/crystallize_shard/crystallize_shard.png");
	}

	@Override
	public CrystallizeShardEntityState createRenderState() {
		return new CrystallizeShardEntityState();
	}

	@Override
	public void extractRenderState(CrystallizeShardEntity crystallizeShard, CrystallizeShardEntityState state, float f) {
		super.extractRenderState(crystallizeShard, state, f);

		state.element = crystallizeShard.getElement();
		state.idleAnimationState = crystallizeShard.idleAnimationState;
	}

	@Override
	public void submit(CrystallizeShardEntityState state, PoseStack matrixStack, SubmitNodeCollector orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
		if (state.element == null) return;

		this.shadowStrength = 0f;
		this.shadowRadius = 0f;

		super.submit(state, matrixStack, orderedRenderCommandQueue, cameraRenderState);
	}

	@Override
	protected void submitNameDisplay(CrystallizeShardEntityState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {}
}
