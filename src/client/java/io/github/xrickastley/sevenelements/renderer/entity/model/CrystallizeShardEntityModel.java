package io.github.xrickastley.sevenelements.renderer.entity.model;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.renderer.entity.state.CrystallizeShardEntityState;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class CrystallizeShardEntityModel extends EntityModel<CrystallizeShardEntityState> {
	public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(SevenElements.identifier("crystallize_shard"), "crystal");

	private static final AnimationDefinition IDLE_ANIMATION = AnimationDefinition.Builder.withLength(3.0F).looping()
		.addAnimation("crystal", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 360.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("particle1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 720.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("particle2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 720.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("particle3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 720.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("particle4", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 720.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.build();

	private final KeyframeAnimation idleAnimation;

	public CrystallizeShardEntityModel(ModelPart root) {
		super(root);

		this.idleAnimation = CrystallizeShardEntityModel.IDLE_ANIMATION.bake(root);
	}

	public static LayerDefinition getTexturedModelData() {
		final MeshDefinition modelData = new MeshDefinition();
		final PartDefinition modelPartData = modelData.getRoot();
		final PartDefinition crystal = modelPartData.addOrReplaceChild("crystal", CubeListBuilder.create(), PartPose.offset(0.0F, 15.0F, -0.5F));

		final PartDefinition shard = crystal.addOrReplaceChild("shard", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		shard.addOrReplaceChild("shard_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -1.0F, 1.25F, 0.4656F, 0.422F, -0.6879F));
		crystal.addOrReplaceChild("particle1", CubeListBuilder.create().texOffs(10, 14).addBox(-1.2313F, -1.0783F, -7.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 0.0F, -1.0908F, 0.5672F));

		final PartDefinition particle2 = crystal.addOrReplaceChild("particle2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 0.0F, 0.0F, -0.5672F));

		particle2.addOrReplaceChild("particle2_r1", CubeListBuilder.create().texOffs(10, 14).addBox(-0.5F, -0.5F, -7.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2686F, -0.5783F, 0.0F, 0.0F, 0.2618F, 0.0F));

		final PartDefinition particle3 = crystal.addOrReplaceChild("particle3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 0.0F, 0.0F, 0.5672F));

		particle3.addOrReplaceChild("particle3_r1", CubeListBuilder.create().texOffs(10, 15).addBox(0.25F, -1.5F, 7.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4814F, 0.4217F, 0.0F, 0.0F, -0.2618F, 0.0F));

		final PartDefinition particle4 = crystal.addOrReplaceChild("particle4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 0.0F, 0.0F, -0.5672F));

		particle4.addOrReplaceChild("particle4_r1", CubeListBuilder.create().texOffs(10, 15).addBox(-0.5F, -0.5F, 7.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2686F, -0.5783F, 0.0F, 0.0F, 0.3054F, 0.0F));

		return LayerDefinition.create(modelData, 16, 16);
	}

	@Override
	public void setupAnim(CrystallizeShardEntityState state) {
		super.setupAnim(state);

		this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
	}
}
