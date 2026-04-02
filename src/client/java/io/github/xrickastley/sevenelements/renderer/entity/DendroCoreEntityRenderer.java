package io.github.xrickastley.sevenelements.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.renderer.entity.model.DendroCoreEntityModel;
import io.github.xrickastley.sevenelements.renderer.entity.state.DendroCoreEntityState;
import io.github.xrickastley.sevenelements.util.Ease;
import io.github.xrickastley.sevenelements.util.MathHelper2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;

public class DendroCoreEntityRenderer extends LivingEntityRenderer<DendroCoreEntity, DendroCoreEntityState, DendroCoreEntityModel> {
	public DendroCoreEntityRenderer(EntityRendererProvider.Context context) {
		super(
			context,
			DendroCoreEntityRenderer.createModel(context),
			0.5f
		);
	}

	private static DendroCoreEntityModel createModel(EntityRendererProvider.Context context) {
		return new DendroCoreEntityModel(context.bakeLayer(DendroCoreEntityModel.MODEL_LAYER));
	}

	@Override
	public Identifier getTextureLocation(DendroCoreEntityState state) {
		return SevenElements.identifier("textures/entity/dendro_core/dendro_core.png");
	}

	@Override
	public DendroCoreEntityState createRenderState() {
		return new DendroCoreEntityState();
	}

	@Override
	public void extractRenderState(DendroCoreEntity dendroCore, DendroCoreEntityState state, float f) {
		super.extractRenderState(dendroCore, state, f);

		state.apply(dendroCore);
	}

	@Override
	public void submit(DendroCoreEntityState livingEntityRenderState, PoseStack matrixStack, SubmitNodeCollector orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
		this.shadowStrength = 0f;
		this.shadowRadius = 0f;

		super.submit(livingEntityRenderState, matrixStack, orderedRenderCommandQueue, cameraRenderState);
	}

	@Override
	protected void scale(DendroCoreEntityState state, PoseStack matrices) {
		super.scale(state, matrices);

		final float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
		final double explodeProgress = Ease.IN_QUAD.applyLerp(MathHelper2.endOffset(state.ageInTicks + delta, 2, 0, 120), 0, 1.5);
		final float scale = !state.isHyperbloom()
			? 0.5f + (float) (explodeProgress * 5)
			: 0.35f;

		matrices.translate(0, 0, 0);
		matrices.scale(scale, scale, scale);
	}

	@Override
	protected void submitNameTag(DendroCoreEntityState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraRenderState) {}
}
