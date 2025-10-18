package io.github.xrickastley.sevenelements.component;

import com.mojang.serialization.Codec;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;

import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.MathHelper;

public final class FrozenEffectComponentImpl implements FrozenEffectComponent {
	private static final Codec<EntityPose> ENTITY_POSE_CODEC = Codec.INT.xmap(EntityPose.INDEX_TO_VALUE::apply, EntityPose::getIndex);

	private final LivingEntity owner;
	private boolean isFrozen = false;
	private boolean hadNoAi = false;
	private EntityPose forcePose = EntityPose.STANDING;
	private float forceHeadYaw = 0.0f;
	private float forceBodyYaw = 0.0f;
	private float forcePitch = 0.0f;
	private float forceLimbAngle = 0.0f;
	private float forceLimbDistance = 0.0f;
	private int ticksFrozen = 0;

	public FrozenEffectComponentImpl(LivingEntity owner) {
		this.owner = owner;
	}

	@Override
	public void readData(ReadView view) {
		this.isFrozen = view.getBoolean("IsFrozen", this.isFrozen);
		this.hadNoAi = view.getBoolean("HadNoAi", this.hadNoAi);
		this.forcePose = view.read("ForcePose", ENTITY_POSE_CODEC).orElse(this.forcePose);
		this.forceHeadYaw = view.getFloat("ForceHeadYaw", this.forceHeadYaw);
		this.forceBodyYaw = view.getFloat("ForceBodyYaw", this.forceBodyYaw);
		this.forcePitch = view.getFloat("ForcePitch", this.forcePitch);
		this.forceLimbAngle = view.getFloat("ForceLimbAngle", this.forceLimbAngle);
		this.forceLimbDistance = view.getFloat("ForceLimbDistance", this.forceLimbDistance);
		this.ticksFrozen = view.getInt("TicksFrozen", this.ticksFrozen);
	}

	@Override
	public void writeData(WriteView view) {
		view.putBoolean("IsFrozen", this.isFrozen);
		view.putBoolean("HadNoAi", this.hadNoAi);
		view.put("ForcePose", ENTITY_POSE_CODEC, this.forcePose);
		view.putFloat("ForceHeadYaw", this.forceHeadYaw);
		view.putFloat("ForceBodyYaw", this.forceBodyYaw);
		view.putFloat("ForcePitch", this.forcePitch);
		view.putFloat("ForceLimbAngle", this.forceLimbAngle);
		view.putFloat("ForceLimbDistance", this.forceLimbDistance);
		view.putInt("TicksFrozen", this.ticksFrozen);
	}

	@Override
	public void clientTick() {
		if (!this.owner.hasStatusEffect(SevenElementsStatusEffects.FROZEN) && this.isFrozen)
			this.unfreeze();

		if (!this.isFrozen()) return;

		owner.setPose(this.forcePose);
		owner.setHeadYaw(this.forceBodyYaw);
		owner.setBodyYaw(this.forceBodyYaw);
		owner.setPitch(this.forcePitch);
	}

	@Override
	public void serverTick() {
		if (!this.owner.hasStatusEffect(SevenElementsStatusEffects.FROZEN) && this.isFrozen)
			this.unfreeze();
	}

	public boolean isFrozen() {
		return this.isFrozen;
	}

	public EntityPose getForcePose() {
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
		this.hadNoAi = owner instanceof final MobEntity mob && mob.isAiDisabled();
		this.forcePose = owner.getPose();
		this.forceHeadYaw = owner.getHeadYaw();
		this.forceBodyYaw = owner.getBodyYaw();
		this.forcePitch = owner.getPitch();
		this.forceLimbAngle = MathHelper.nextFloat(owner.getRandom(), 0, 0.5f);
		this.forceLimbDistance = MathHelper.nextFloat(owner.getRandom(), -0.5f, 0.5f);
		this.ticksFrozen = owner.getFrozenTicks();

		owner.setSilent(true);

		if (owner instanceof final MobEntity mob) mob.setAiDisabled(true);

		FrozenEffectComponent.sync(owner);
	}

	public void unfreeze() {
		if (!this.isFrozen) return;

		this.isFrozen = false;

		owner.setSilent(false);

		if (owner instanceof final MobEntity mob) mob.setAiDisabled(this.hadNoAi);

		owner.setFrozenTicks(this.ticksFrozen);

		FrozenEffectComponent.sync(owner);
	}
}
