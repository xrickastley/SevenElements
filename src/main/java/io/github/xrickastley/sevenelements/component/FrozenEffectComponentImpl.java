package io.github.xrickastley.sevenelements.component;

import com.mojang.serialization.Codec;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.util.NbtHelper;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
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
	public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registry) {
		this.isFrozen = NbtHelper.get(tag, "IsFrozen", Codec.BOOL);
		this.hadNoAi = NbtHelper.get(tag, "HadNoAi", Codec.BOOL);
		this.forcePose = NbtHelper.get(tag, "ForcePose", ENTITY_POSE_CODEC);
		this.forceHeadYaw = NbtHelper.get(tag, "ForceHeadYaw", Codec.FLOAT);
		this.forceBodyYaw = NbtHelper.get(tag, "ForceBodyYaw", Codec.FLOAT);
		this.forcePitch = NbtHelper.get(tag, "ForcePitch", Codec.FLOAT);
		this.forceLimbAngle = NbtHelper.get(tag, "ForceLimbAngle", Codec.FLOAT);
		this.forceLimbDistance = NbtHelper.get(tag, "ForceLimbDistance", Codec.FLOAT);
		this.ticksFrozen = NbtHelper.get(tag, "TicksFrozen", Codec.INT);
	}

	@Override
	public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registry) {
		tag.putBoolean("IsFrozen", this.isFrozen);
		tag.putBoolean("HadNoAi", this.hadNoAi);
		tag.put("ForcePose", ENTITY_POSE_CODEC, this.forcePose);
		tag.putFloat("ForceHeadYaw", this.forceHeadYaw);
		tag.putFloat("ForceBodyYaw", this.forceBodyYaw);
		tag.putFloat("ForcePitch", this.forcePitch);
		tag.putFloat("ForceLimbAngle", this.forceLimbAngle);
		tag.putFloat("ForceLimbDistance", this.forceLimbDistance);
		tag.putInt("TicksFrozen", this.ticksFrozen);
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
