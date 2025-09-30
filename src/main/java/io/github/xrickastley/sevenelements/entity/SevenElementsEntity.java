package io.github.xrickastley.sevenelements.entity;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Arm;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

// Base class for all "special" entities: Crystallize Shard and Dendro Core
// Should technically extend Entity, but extends LivingEntity instead to NOT deal with more Networking and Spawn Packets.
public sealed class SevenElementsEntity
	extends LivingEntity
	permits DendroCoreEntity, CrystallizeShardEntity
{
	protected SevenElementsEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	public static DefaultAttributeContainer.Builder getAttributeBuilder() {
		return LivingEntity.createLivingAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 1);
	}

	public int getAge() {
		return this.age;
	}

	@Override
	public Arm getMainArm() {
		return Arm.LEFT;
	}

	@Override
	public void setPose(EntityPose pose) {
		super.setPose(EntityPose.STANDING);
	}

	@Override
	public EntityPose getPose() {
		return EntityPose.STANDING;
	}

	@Override
	public Iterable<ItemStack> getArmorItems() {
		return DefaultedList.of();
	}

	@Override
	public void equipStack(EquipmentSlot slot, ItemStack stack) {}

	@Override
	public ItemStack getEquippedStack(EquipmentSlot slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canHaveStatusEffect(StatusEffectInstance effect) {
		return false;
	}

	@Override
	public boolean addStatusEffect(StatusEffectInstance effect, Entity source) {
		return false;
	}

	@Override
	public void kill() {
		this.remove(RemovalReason.KILLED);
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean collidesWith(Entity other) {
		return false;
	}

	@Override
	public boolean isCollidable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void pushAwayFrom(Entity entity) {}

	@Override
	public void takeKnockback(double strength, double x, double z) {}

	protected final @Nullable LivingEntity getEntityFromUUID(UUID uuid) {
		return this.getWorld() instanceof final ServerWorld world
			? ClassInstanceUtil.castOrNull(world.getEntity(uuid), LivingEntity.class)
			: null;
	}
}
