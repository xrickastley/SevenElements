package io.github.xrickastley.sevenelements.entity;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Base class for all "special" entities: Crystallize Shard and Dendro Core
// Should technically extend Entity, but extends LivingEntity instead to NOT deal with more Networking and Spawn Packets.
public sealed class SevenElementsEntity
	extends LivingEntity
	permits DendroCoreEntity, CrystallizeShardEntity
{
	protected SevenElementsEntity(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
	}

	public static AttributeSupplier.Builder getAttributeBuilder() {
		return LivingEntity.createLivingAttributes()
			.add(Attributes.MAX_HEALTH, 1);
	}

	public int getAge() {
		return this.tickCount;
	}

	@Override
	public HumanoidArm getMainArm() {
		return HumanoidArm.LEFT;
	}

	@Override
	public void setPose(Pose pose) {
		super.setPose(Pose.STANDING);
	}

	@Override
	public Pose getPose() {
		return Pose.STANDING;
	}

	@Override
	public void setItemSlot(EquipmentSlot slot, ItemStack stack) {}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canBeAffected(MobEffectInstance effect) {
		return false;
	}

	@Override
	public boolean addEffect(MobEffectInstance effect, Entity source) {
		return false;
	}

	@Override
	public void kill(ServerLevel world) {
		this.remove(RemovalReason.KILLED);
	}

	@Override
	public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean canCollideWith(Entity other) {
		return false;
	}

	@Override
	public boolean canBeCollidedWith(Entity entity) {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void push(Entity entity) {}

	@Override
	public void knockback(double strength, double x, double z) {}

	protected final @Nullable LivingEntity getEntityFromUUID(UUID uuid) {
		return this.level() instanceof final ServerLevel world
			? ClassInstanceUtil.castOrNull(world.getEntity(uuid), LivingEntity.class)
			: null;
	}
}
