package io.github.xrickastley.sevenelements.renderer.entity.model;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.renderer.entity.state.DendroCoreEntityState;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class DendroCoreEntityModel extends EntityModel<DendroCoreEntityState> {
	public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(SevenElements.identifier("dendro_core"), "all");

	private final ModelPart bone;

	public DendroCoreEntityModel(ModelPart root) {
		super(root);

		this.bone = root.getChild("bone");
	}

	public static LayerDefinition getTexturedModelData() {
		final MeshDefinition modelData = new MeshDefinition();

		final PartDefinition modelPartData = modelData.getRoot();
		final PartDefinition bone = modelPartData.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(21, 2).addBox(-0.5F, -15.0F, -6.5F, 0.0F, 17.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(21, 15).addBox(-7.0F, -15.0F, 0.0F, 13.0F, 17.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(25, 2).addBox(-1.5F, -14.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 22.0F, 0.0F));

		bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 5).addBox(0.0F, -10.0F, -5.0F, 0.0F, 12.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(0, 15).addBox(-5.0F, -10.0F, 0.0F, 10.0F, 12.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		return LayerDefinition.create(modelData, 48, 48);
	}

	@Override
	public void setupAnim(DendroCoreEntityState state) {
		float progress = state.ageInTicks % 60 == 0
			? 1
			: state.ageInTicks % 60;

		bone.setRotation(0, -progress * 0.05f, 0);
	}
}
