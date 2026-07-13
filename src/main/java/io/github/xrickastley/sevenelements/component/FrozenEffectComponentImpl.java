package io.github.xrickastley.sevenelements.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class FrozenEffectComponentImpl implements FrozenEffectComponent {
	private static final Codec<Pose> ENTITY_POSE_CODEC = Codec.withAlternative(
		Codec.INT.xmap(Pose.BY_ID::apply, Pose::id),
		Codec.STRING.comapFlatMap(FrozenEffectComponentImpl::validatePose, Pose::toString)
	);

	private final LivingEntity owner;
	private boolean isFrozen = false;
	private boolean hadNoAi = false;
	private Pose forcePose = Pose.STANDING;
	private float forceHeadYaw = 0.0f;
	private float forceBodyYaw = 0.0f;
	private float forcePitch = 0.0f;
	private float forceLimbAngle = 0.0f;
	private float forceLimbDistance = 0.0f;
	private int ticksFrozen = 0;

	public FrozenEffectComponentImpl(LivingEntity owner) {
		this.owner = owner;
	}

	private static final DataResult<Pose> validatePose(String entityPose) {
		try {
			return DataResult.success(Pose.valueOf(entityPose));
		} catch (IllegalArgumentException e) {
			return DataResult.error(() -> "Not a valid EntityPose: " + entityPose + " " + e.getMessage());
		}
	}

	@Override
	public void readData(ValueInput view) {
		this.isFrozen = view.getBooleanOr("IsFrozen", this.isFrozen);
		this.hadNoAi = view.getBooleanOr("HadNoAi", this.hadNoAi);
		this.forcePose = view.read("ForcePose", ENTITY_POSE_CODEC).orElse(this.forcePose);
		this.forceHeadYaw = view.getFloatOr("ForceHeadYaw", this.forceHeadYaw);
		this.forceBodyYaw = view.getFloatOr("ForceBodyYaw", this.forceBodyYaw);
		this.forcePitch = view.getFloatOr("ForcePitch", this.forcePitch);
		this.forceLimbAngle = view.getFloatOr("ForceLimbAngle", this.forceLimbAngle);
		this.forceLimbDistance = view.getFloatOr("ForceLimbDistance", this.forceLimbDistance);
		this.ticksFrozen = view.getIntOr("TicksFrozen", this.ticksFrozen);
	}

	@Override
	public void writeData(ValueOutput view) {
		view.putBoolean("IsFrozen", this.isFrozen);
		view.putBoolean("HadNoAi", this.hadNoAi);
		view.store("ForcePose", ENTITY_POSE_CODEC, this.forcePose);
		view.putFloat("ForceHeadYaw", this.forceHeadYaw);
		view.putFloat("ForceBodyYaw", this.forceBodyYaw);
		view.putFloat("ForcePitch", this.forcePitch);
		view.putFloat("ForceLimbAngle", this.forceLimbAngle);
		view.putFloat("ForceLimbDistance", this.forceLimbDistance);
		view.putInt("TicksFrozen", this.ticksFrozen);
	}

	@Override
	public void clientTick() {
		if (!this.owner.hasEffect(SevenElementsStatusEffects.FROZEN) && this.isFrozen)
			this.unfreeze();

		if (!this.isFrozen()) return;

		owner.setPose(this.forcePose);
		owner.setYHeadRot(this.forceBodyYaw);
		owner.setYBodyRot(this.forceBodyYaw);
		owner.setXRot(this.forcePitch);
	}

	@Override
	public void serverTick() {
		if (!this.owner.hasEffect(SevenElementsStatusEffects.FROZEN) && this.isFrozen)
			this.unfreeze();

		if (!this.isFrozen()) return;

		owner.setPose(this.forcePose);
		owner.setYHeadRot(this.forceBodyYaw);
		owner.setYBodyRot(this.forceBodyYaw);
		owner.setXRot(this.forcePitch);
	}

	public boolean isFrozen() {
		return this.isFrozen;
	}

	public Pose getForcePose() {
		return this.forcePose;
	}

	public float getForceHeadYaw() {
		return this.forceHeadYaw;
	}

	public float getForceBodyYaw() {
		return this.forceBodyYaw;
	}

	public float getForcePitch() {
		return this.forcePitch;
	}

	public float getForceLimbAngle() {
		return this.forceLimbAngle;
	}

	public float getForceLimbDistance() {
		return this.forceLimbDistance;
	}

	public void freeze() {
		if (this.isFrozen) return;

		this.isFrozen = true;
		this.hadNoAi = owner instanceof final Mob mob && mob.isNoAi();
		this.forcePose = owner.getPose();
		this.forceHeadYaw = owner.getYHeadRot();
		this.forceBodyYaw = owner.getVisualRotationYInDegrees();
		this.forcePitch = owner.getXRot();
		this.forceLimbAngle = Mth.nextFloat(owner.getRandom(), 0, 0.5f);
		this.forceLimbDistance = Mth.nextFloat(owner.getRandom(), -0.5f, 0.5f);
		this.ticksFrozen = owner.getTicksFrozen();

		owner.setSilent(true);

		if (owner instanceof final Mob mob) mob.setNoAi(true);

		FrozenEffectComponent.sync(owner);
	}

	public void unfreeze() {
		if (!this.isFrozen) return;

		this.isFrozen = false;

		owner.setSilent(false);

		if (owner instanceof final Mob mob) mob.setNoAi(this.hadNoAi);

		owner.setTicksFrozen(this.ticksFrozen);

		FrozenEffectComponent.sync(owner);
	}
}
