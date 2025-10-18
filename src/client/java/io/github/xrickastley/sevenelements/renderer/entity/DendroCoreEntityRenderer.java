package io.github.xrickastley.sevenelements.renderer.entity;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.renderer.entity.model.DendroCoreEntityModel;
import io.github.xrickastley.sevenelements.renderer.entity.state.DendroCoreEntityState;
import io.github.xrickastley.sevenelements.util.Ease;
import io.github.xrickastley.sevenelements.util.MathHelper2;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class DendroCoreEntityRenderer extends LivingEntityRenderer<DendroCoreEntity, DendroCoreEntityState, DendroCoreEntityModel> {
	public DendroCoreEntityRenderer(EntityRendererFactory.Context context) {
		super(
			context,
			DendroCoreEntityRenderer.createModel(context),
			0.5f
		);
	}

	private static DendroCoreEntityModel createModel(EntityRendererFactory.Context context) {
		return new DendroCoreEntityModel(context.getPart(DendroCoreEntityModel.MODEL_LAYER));
	}

	@Override
	public Identifier getTexture(DendroCoreEntityState state) {
		return SevenElements.identifier("textures/entity/dendro_core/dendro_core.png");
	}

	@Override
	public DendroCoreEntityState createRenderState() {
		return new DendroCoreEntityState();
	}

	@Override
	public void updateRenderState(DendroCoreEntity dendroCore, DendroCoreEntityState state, float f) {
		super.updateRenderState(dendroCore, state, f);

		state.apply(dendroCore);
	}
	
	@Override
	public void render(DendroCoreEntityState livingEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
		this.shadowOpacity = 0f;
		this.shadowRadius = 0f;
		
		super.render(livingEntityRenderState, matrixStack, orderedRenderCommandQueue, cameraRenderState);
	}

	@Override
	protected void scale(DendroCoreEntityState state, MatrixStack matrices) {
		super.scale(state, matrices);

		final float delta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false);
		final double explodeProgress = Ease.IN_QUAD.applyLerp(MathHelper2.endOffset(state.age + delta, 2, 0, 120), 0, 1.5);
		final float scale = !state.isHyperbloom()
			? 0.5f + (float) (explodeProgress * 5)
			: 0.35f;

		matrices.translate(0, 0, 0);
		matrices.scale(scale, scale, scale);
	}

	@Override
	protected void renderLabelIfPresent(DendroCoreEntityState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {}
}
