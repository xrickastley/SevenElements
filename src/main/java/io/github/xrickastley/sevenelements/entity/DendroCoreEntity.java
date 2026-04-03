package io.github.xrickastley.sevenelements.entity;

import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReactions;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypes;
import io.github.xrickastley.sevenelements.registry.SevenElementsEntityTypeTags;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.ViewHelper;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// Should technically extend Entity, but extends LivingEntity instead to NOT deal with more Networking and Spawn Packets.
public final class DendroCoreEntity extends SevenElementsEntity {
	private static final double SPRAWLING_SHOT_SPEED = 0.75;
	private static final double SPRAWLING_SHOT_GRAVITY = -0.05;
	private static final double SPRAWLING_SHOT_RADIUS = 24;
	private static final int SPRAWLING_SHOT_DELAY = 6;
	private static final double DENDRO_CORES_IN_RADIUS = 64;

	private List<UUID> owners;
	private @Nullable UUID target;
	private Type type = Type.NORMAL;
	private boolean exploded = false;
	private int hyperbloomAge = 0;
	private int curTicksInHitbox = 0;
	private boolean direct = false;

	public DendroCoreEntity(EntityType<? extends LivingEntity> entityType, Level world) {
		this(entityType, world, null);
	}

	public DendroCoreEntity(EntityType<? extends LivingEntity> entityType, Level world, @Nullable LivingEntity owner) {
		super(entityType, world);

		this.owners = new ArrayList<>();
		if (owner != null) this.owners.add(owner.getUUID());
	}

	public DendroCoreEntity setOwner(LivingEntity owner) {
		this.owners = new ArrayList<>();

		if (owner != null) this.owners.add(owner.getUUID());

		return this;
	}

	public DendroCoreEntity addOwner(LivingEntity owner) {
		if (owner != null) this.owners.add(owner.getUUID());

		return this;
	}

	public void setAsHyperbloom() {
		if (this.type != Type.NORMAL) throw new IllegalStateException("This DendroCoreEntity has already been transformed! Type: " + this.type);

		this.type = Type.HYPERBLOOM;
		this.hyperbloomAge = this.tickCount;
		this.noPhysics = true;
		this.setNoGravity(true);

		final @Nullable LivingEntity target = ElementalReaction
			.getEntitiesInAoE(this, DendroCoreEntity.SPRAWLING_SHOT_RADIUS)
			.stream()
			.filter(e -> !(this.owners.contains(e.getUUID()) || e.isDeadOrDying() || e instanceof SevenElementsEntity || e.is(SevenElementsEntityTypeTags.IGNORED_TARGETS) || e.hasInfiniteMaterials()))
			.min(Comparator.comparing(e -> e.distanceToSqr(this)))
			.orElse(null);

		if (target == null) return;

		this.target = target.getUUID();
		this.sendStateUpdate();
	}

	public void setAsBurgeon() {
		if (this.type != Type.NORMAL) throw new IllegalStateException("This DendroCoreEntity has already been transformed! Type: " + this.type);

		this.type = Type.BURGEON;
		this.explode(3.0);
	}

	public boolean isNormal() {
		return this.type == Type.NORMAL;
	}

	public boolean isHyperbloom() {
		return this.type == Type.HYPERBLOOM;
	}

	public boolean isBurgeon() {
		return this.type == Type.BURGEON;
	}

	@Override
	public void addAdditionalSaveData(ValueOutput view) {
		super.addAdditionalSaveData(view);

		view.store("Type", DendroCoreEntity.Type.CODEC, this.type);
		view.putBoolean("Direct", this.direct);
		view.storeNullable("Target", UUIDUtil.AUTHLIB_CODEC, target);

		ViewHelper.putList(view, "Owners", UUIDUtil.AUTHLIB_CODEC, this.owners);
	}

	@Override
	public void readAdditionalSaveData(ValueInput view) {
		super.readAdditionalSaveData(view);

		this.type = view.read("Type", DendroCoreEntity.Type.CODEC).orElse(Type.NORMAL);
		this.direct = view.read("Direct", Codec.BOOL).orElse(this.direct);
		this.target = view.read("Target", UUIDUtil.AUTHLIB_CODEC).orElse(null);

		this.owners.clear();
		this.owners.addAll(ViewHelper.getList(view, "Owners", UUIDUtil.AUTHLIB_CODEC));
	}

	private void doHyperbloom() {
		if (!(this.level() instanceof final ServerLevel world)) return;

		final int hyperbloomTick = this.tickCount - this.hyperbloomAge;
		final LivingEntity target = ClassInstanceUtil.castOrNull(world.getEntity(this.target), LivingEntity.class);

		if (target != null) {
			final Vec3 targetPos = target.getEyePosition().subtract(this.position());
			final double distance = Math.sqrt(targetPos.x * targetPos.x + targetPos.z * targetPos.z);
			final int ticks = Math.max(1, (int) (distance / DendroCoreEntity.SPRAWLING_SHOT_SPEED));

			if (ticks <= 5) this.direct = true;

			// y value is derived from y(t) = y_0 + v_yt + \frac{1}{2}ay \times t^2
			final Vec3 velocity = new Vec3(
				targetPos.x / ticks,
				direct
					? targetPos.y / ticks
					: (targetPos.y - 0.5 * DendroCoreEntity.SPRAWLING_SHOT_GRAVITY * ticks * ticks) / ticks,
				targetPos.z / ticks
			);

			super.setDeltaMovement(velocity);

			final AABB boundingBox = target.getBoundingBox();

			if (!boundingBox.contains(this.position())) return;

			this.curTicksInHitbox++;

			if (this.curTicksInHitbox < DendroCoreEntity.SPRAWLING_SHOT_DELAY) return;

			for (final Entity target2 : ElementalReaction.getEntitiesInAoE(target, 1.0, e -> !owners.contains(e.getUUID())))
				target2.hurtServer(world, this.createDamageSource(target), ElementalReaction.getReactionDamage(this, 3.0));

			this.remove(RemovalReason.KILLED);

			this.level()
				.playSound(null, this.blockPosition(), SevenElementsSoundEvents.SPRAWLING_SHOT_HIT, SoundSource.PLAYERS, 0.5f, 1.0f);
		} else {
			super.setDeltaMovement(new Vec3(0, 0.5, 0));

			if (hyperbloomTick >= 40) this.remove(RemovalReason.KILLED);
		}
	}

	@Override
	public void kill(ServerLevel world) {
		this.explode(2.0);
	}

	@Override
	public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
		source = ElementComponent.applyElementalInfusions(source, this);

		if (!(source instanceof final ElementalDamageSource eds) || !this.isNormal()) return false;

		final Element element = eds.getElementalApplication().getElement();

		if (element != Element.PYRO && element != Element.ELECTRO) return false;

		final ElementalReaction reaction = element == Element.PYRO
			? ElementalReactions.BURGEON
			: ElementalReactions.HYPERBLOOM;

		reaction.trigger(this, ClassInstanceUtil.castOrNull(source.getEntity(), LivingEntity.class));

		return true;
	}

	@Override
	public void tick() {
		super.tick();

		if (this.tickCount == 1) this.removeOldDendroCores();

		if (this.type == Type.HYPERBLOOM) this.doHyperbloom();

		if (this.tickCount >= 120 && type != Type.HYPERBLOOM) {
			this.explode(2.0);
			this.remove(RemovalReason.KILLED);
		}
	}

	public void syncFromPacket(SyncDendroCoreStateS2CPayload packet) {
		this.type = packet.type;
		this.tickCount = packet.age;
	}

	private void removeOldDendroCores() {
		if (!(this.level() instanceof final ServerLevel world)) return;

		final AABB box = AABB.ofSize(this.getPosition(1f), DendroCoreEntity.DENDRO_CORES_IN_RADIUS, DendroCoreEntity.DENDRO_CORES_IN_RADIUS, DendroCoreEntity.DENDRO_CORES_IN_RADIUS);
		final List<DendroCoreEntity> dendroCores = this.level().getEntitiesOfClass(DendroCoreEntity.class, box, dc -> true);

		if (dendroCores.size() <= 5) return;

		dendroCores.sort(Comparator.comparing(DendroCoreEntity::getAge).reversed());

		final Queue<DendroCoreEntity> queue = new LinkedList<>(dendroCores);

		while (queue.peek() != null && queue.size() > 5) queue.remove().kill(world);
	}

	private boolean explode(final double reactionMultiplier) {
		if (!(this.level() instanceof final ServerLevel world)) return false;

		if (this.exploded) return false;

		this.exploded = true;
		this.tickCount = 117;
		this.sendStateUpdate();

		if (!this.level().isClientSide()) this.sendStateUpdate();

		final @Nullable LivingEntity recentOwner = this.getRecentOwner();

		for (final LivingEntity target : ElementalReaction.getEntitiesInAoE(this, 5.0)) {
			if (target instanceof DendroCoreEntity) continue;

			final ElementalDamageSource source = this.createDamageSource(target, recentOwner);

			float damage = ElementalReaction.getReactionDamage(this, reactionMultiplier);

			if (this.owners.contains(target.getUUID())) damage *= 0.02f;

			target.hurtServer(world, source, damage);
		}

		this.level()
			.playSound(null, this.blockPosition(), SevenElementsSoundEvents.DENDRO_CORE_EXPLOSION, SoundSource.PLAYERS, 0.5f, 1.0f);

		return true;
	}

	private @Nullable LivingEntity getRecentOwner() {
		return !owners.isEmpty() && this.level() instanceof ServerLevel
			? this.getEntityFromUUID(owners.get(owners.size() - 1))
			: null;
	}

	private ElementalDamageSource createDamageSource(final LivingEntity target) {
		final @Nullable LivingEntity recentOwner = this.getRecentOwner();

		return this.createDamageSource(target, recentOwner);
	}

	private ElementalDamageSource createDamageSource(final LivingEntity target, final LivingEntity recentOwner) {
		return new ElementalDamageSource(
			this.level()
				.damageSources()
				.source(SevenElementsDamageTypes.DENDRO_CORE, this, recentOwner),
			ElementalApplications.gaugeUnits(target, Element.DENDRO, 0.0),
			InternalCooldownContext.ofNone(recentOwner)
		).shouldApplyDMGBonus(false);
	}

	private void sendStateUpdate() {
		if (this.level().isClientSide()) return;

		final SyncDendroCoreStateS2CPayload packet = new SyncDendroCoreStateS2CPayload(this);

		for (final ServerPlayer otherPlayer : PlayerLookup.tracking(this))
			ServerPlayNetworking.send(otherPlayer, packet);
	}

	static {
		ElementComponent.denyElementsFor(DendroCoreEntity.class);
	}

	private static enum Type {
		NORMAL, HYPERBLOOM, BURGEON;

		private static final Codec<Type> CODEC = ExtraCodecs.NON_EMPTY_STRING.xmap(Type::valueOf, Type::toString);
	}

	public static class SyncDendroCoreStateS2CPayload implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<SyncDendroCoreStateS2CPayload> ID = new CustomPacketPayload.Type<>(
			SevenElements.identifier("s2c/sync_dendro_core_state")
		);

		public static final StreamCodec<RegistryFriendlyByteBuf, SyncDendroCoreStateS2CPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, SyncDendroCoreStateS2CPayload::entityId,
			ByteBufCodecs.INT, SyncDendroCoreStateS2CPayload::age,
			ByteBufCodecs.fromCodec(DendroCoreEntity.Type.CODEC), SyncDendroCoreStateS2CPayload::dendroCoreType,
			SyncDendroCoreStateS2CPayload::new
		);

		private final int entityId;
		private final int age;
		private final DendroCoreEntity.Type type;

		public SyncDendroCoreStateS2CPayload(final DendroCoreEntity dendroCore) {
			this(dendroCore.getId(), dendroCore.tickCount, dendroCore.type);
		}

		private SyncDendroCoreStateS2CPayload(int entityId, int age, DendroCoreEntity.Type type) {
			this.entityId = entityId;
			this.age = age;
			this.type = type;
		}

		public int entityId() {
			return this.entityId;
		}

		private int age() {
			return this.age;
		}

		public DendroCoreEntity.Type dendroCoreType() {
			return type;
		}

		@Override
		public CustomPacketPayload.Type<SyncDendroCoreStateS2CPayload> type() {
			return ID;
		}
	}
}
