package io.github.xrickastley.sevenelements.renderer.entity.model;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.renderer.entity.state.CrystallizeShardEntityState;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public class CrystallizeShardEntityModel extends EntityModel<CrystallizeShardEntityState> {
	public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(SevenElements.identifier("crystallize_shard"), "crystal");

	private static final Animation IDLE_ANIMATION = Animation.Builder.create(3.0F).looping()
		.addBoneAnimation("crystal", new Transformation(Transformation.Targets.ROTATE,
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.0F, AnimationHelper.createRotationalVector(0.0F, 360.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation("particle1", new Transformation(Transformation.Targets.ROTATE,
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.0F, AnimationHelper.createRotationalVector(0.0F, 720.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation("particle2", new Transformation(Transformation.Targets.ROTATE,
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.0F, AnimationHelper.createRotationalVector(0.0F, 720.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation("particle3", new Transformation(Transformation.Targets.ROTATE,
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.0F, AnimationHelper.createRotationalVector(0.0F, 720.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation("particle4", new Transformation(Transformation.Targets.ROTATE,
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.0F, AnimationHelper.createRotationalVector(0.0F, 720.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.build();

	public CrystallizeShardEntityModel(ModelPart root) {
		super(root);
	}

	public static TexturedModelData getTexturedModelData() {
		final ModelData modelData = new ModelData();
		final ModelPartData modelPartData = modelData.getRoot();
		final ModelPartData crystal = modelPartData.addChild("crystal", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 15.0F, -0.5F));

		final ModelPartData shard = crystal.addChild("shard", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

		shard.addChild("shard_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -1.0F, -3.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(-1.5F, -1.0F, 1.25F, 0.4656F, 0.422F, -0.6879F));
		crystal.addChild("particle1", ModelPartBuilder.create().uv(10, 14).cuboid(-1.2313F, -1.0783F, -7.0F, 3.0F, 1.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -0.5F, 0.5F, 0.0F, -1.0908F, 0.5672F));

		final ModelPartData particle2 = crystal.addChild("particle2", ModelPartBuilder.create(), ModelTransform.of(0.0F, -0.5F, 0.5F, 0.0F, 0.0F, -0.5672F));

		particle2.addChild("particle2_r1", ModelPartBuilder.create().uv(10, 14).cuboid(-0.5F, -0.5F, -7.0F, 3.0F, 1.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-0.2686F, -0.5783F, 0.0F, 0.0F, 0.2618F, 0.0F));

		final ModelPartData particle3 = crystal.addChild("particle3", ModelPartBuilder.create(), ModelTransform.of(0.0F, -0.5F, 0.5F, 0.0F, 0.0F, 0.5672F));

		particle3.addChild("particle3_r1", ModelPartBuilder.create().uv(10, 15).cuboid(0.25F, -1.5F, 7.0F, 3.0F, 1.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-0.4814F, 0.4217F, 0.0F, 0.0F, -0.2618F, 0.0F));

		final ModelPartData particle4 = crystal.addChild("particle4", ModelPartBuilder.create(), ModelTransform.of(0.0F, -0.5F, 0.5F, 0.0F, 0.0F, -0.5672F));

		particle4.addChild("particle4_r1", ModelPartBuilder.create().uv(10, 15).cuboid(-0.5F, -0.5F, 7.0F, 3.0F, 1.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-0.2686F, -0.5783F, 0.0F, 0.0F, 0.3054F, 0.0F));

		return TexturedModelData.of(modelData, 16, 16);
	}

	@Override
	public void setAngles(CrystallizeShardEntityState state) {
		super.setAngles(state);

		this.animate(state.idleAnimationState, IDLE_ANIMATION, state.age, 1.5f);
	}
}
