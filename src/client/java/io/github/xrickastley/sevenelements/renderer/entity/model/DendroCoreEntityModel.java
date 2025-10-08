package io.github.xrickastley.sevenelements.renderer.entity.model;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;

public class DendroCoreEntityModel extends EntityModel<DendroCoreEntity> {
	public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(SevenElements.identifier("dendro_core"), "all");

	private final ModelPart bone;

	public DendroCoreEntityModel(ModelPart root) {
		this.bone = root.getChild("bone");
	}

	public static TexturedModelData getTexturedModelData() {
		final ModelData modelData = new ModelData();

		final ModelPartData modelPartData = modelData.getRoot();
		final ModelPartData bone = modelPartData.addChild("bone", ModelPartBuilder.create().uv(21, 2).cuboid(-0.5F, -15.0F, -6.5F, 0.0F, 17.0F, 13.0F, new Dilation(0.0F))
		.uv(21, 15).cuboid(-7.0F, -15.0F, 0.0F, 13.0F, 17.0F, 0.0F, new Dilation(0.0F))
		.uv(25, 2).cuboid(-1.5F, -14.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.5F, 22.0F, 0.0F));

		bone.addChild("cube_r1", ModelPartBuilder.create().uv(0, 5).cuboid(0.0F, -10.0F, -5.0F, 0.0F, 12.0F, 10.0F, new Dilation(0.0F))
		.uv(0, 15).cuboid(-5.0F, -10.0F, 0.0F, 10.0F, 12.0F, 0.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-4.0F, -9.0F, -4.0F, 8.0F, 6.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(-0.5F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		return TexturedModelData.of(modelData, 48, 48);
	}

	@Override
	public void setAngles(DendroCoreEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

	}

	@Override
	public void animateModel(DendroCoreEntity entity, float limbAngle, float limbDistance, float tickDelta) {
		float progress = (entity.age + tickDelta) % 60 == 0
			? 1
			: (entity.age + tickDelta) % 60;

		bone.setAngles(0, -progress * 0.05f, 0);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		bone.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}
}
