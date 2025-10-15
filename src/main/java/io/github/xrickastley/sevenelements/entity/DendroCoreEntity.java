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
import io.github.xrickastley.sevenelements.util.NbtHelper;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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

	public DendroCoreEntity(EntityType<? extends LivingEntity> entityType, World world) {
		this(entityType, world, null);
	}

	public DendroCoreEntity(EntityType<? extends LivingEntity> entityType, World world, @Nullable LivingEntity owner) {
		super(entityType, world);

		this.owners = new ArrayList<>();
		if (owner != null) this.owners.add(owner.getUuid());
	}

	public DendroCoreEntity setOwner(LivingEntity owner) {
		this.owners = new ArrayList<>();

		if (owner != null) this.owners.add(owner.getUuid());

		return this;
	}

	public DendroCoreEntity addOwner(LivingEntity owner) {
		if (owner != null) this.owners.add(owner.getUuid());

		return this;
	}

	public void setAsHyperbloom() {
		if (this.type != Type.NORMAL) throw new IllegalStateException("This DendroCoreEntity has already been transformed! Type: " + this.type);

		this.type = Type.HYPERBLOOM;
		this.hyperbloomAge = this.age;
		this.noClip = true;
		this.setNoGravity(true);

		final @Nullable LivingEntity target = ElementalReaction
			.getEntitiesInAoE(this, DendroCoreEntity.SPRAWLING_SHOT_RADIUS)
			.stream()
			.filter(e -> !(this.owners.contains(e.getUuid()) || e.isDead() || e instanceof SevenElementsEntity || e.getType().isIn(SevenElementsEntityTypeTags.IGNORED_TARGETS)))
			.min(Comparator.comparing(e -> e.squaredDistanceTo(this)))
			.orElse(null);

		if (target == null) return;

		this.target = target.getUuid();
		this.sendSyncPayload();
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

	public void syncFromPayload(SyncDendroCoreS2CPayload payload) {
		this.type = payload.type;
		this.age = payload.age;
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);

		nbt.put("Type", DendroCoreEntity.Type.CODEC, this.type);
		nbt.putNullable("Target", Uuids.CODEC, target);

		NbtHelper.putList(nbt, "Owners", Uuids.CODEC, this.owners);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);

		this.type = nbt.get("Type", DendroCoreEntity.Type.CODEC).orElse(Type.NORMAL);
		this.target = nbt.get("Target", Uuids.CODEC).orElse(null);

		this.owners.clear();
		this.owners.addAll(NbtHelper.getList(nbt, "Owners", Uuids.CODEC));
	}

	private void doHyperbloom() {
		if (!(this.getWorld() instanceof final ServerWorld world)) return;

		final int hyperbloomTick = this.age - this.hyperbloomAge;
		final LivingEntity target = ClassInstanceUtil.castOrNull(world.getEntity(this.target), LivingEntity.class);

		if (target != null) {
			final Vec3d targetPos = target.getEyePos().subtract(this.getPos());
			final double distance = Math.sqrt(targetPos.x * targetPos.x + targetPos.z * targetPos.z);
			final int ticks = Math.max(1, (int) (distance / DendroCoreEntity.SPRAWLING_SHOT_SPEED));

			// y value is derived from y(t) = y_0 + v_yt + \frac{1}{2}ay \times t^2
			final Vec3d velocity = new Vec3d(
				targetPos.x / ticks,
				(targetPos.y - 0.5 * DendroCoreEntity.SPRAWLING_SHOT_GRAVITY * ticks * ticks) / ticks,
				targetPos.z / ticks
			);

			super.setVelocity(velocity);

			final Box boundingBox = target.getBoundingBox();

			if (!boundingBox.contains(this.getPos())) return;

			this.curTicksInHitbox++;

			if (this.curTicksInHitbox < DendroCoreEntity.SPRAWLING_SHOT_DELAY) return;

			for (final Entity target2 : ElementalReaction.getEntitiesInAoE(target, 1.0, e -> !owners.contains(e.getUuid())))
				target2.damage(world, this.createDamageSource(target), ElementalReaction.getReactionDamage(this, 3.0));

			this.remove(RemovalReason.KILLED);

			this.getWorld()
				.playSound(null, this.getBlockPos(), SevenElementsSoundEvents.SPRAWLING_SHOT_HIT, SoundCategory.PLAYERS, 0.5f, 1.0f);
		} else {
			super.setVelocity(new Vec3d(0, 0.5, 0));

			if (hyperbloomTick >= 40) this.remove(RemovalReason.KILLED);
		}
	}

	@Override
	public void kill(ServerWorld world) {
		this.explode(2.0);
	}

	@Override
	public boolean damage(ServerWorld world, DamageSource source, float amount) {
		source = ElementComponent.applyElementalInfusions(source, this);

		if (!(source instanceof final ElementalDamageSource eds) || !this.isNormal()) return false;

		final Element element = eds.getElementalApplication().getElement();

		if (element != Element.PYRO && element != Element.ELECTRO) return false;

		final ElementalReaction reaction = element == Element.PYRO
			? ElementalReactions.BURGEON
			: ElementalReactions.HYPERBLOOM;

		reaction.trigger(this, ClassInstanceUtil.castOrNull(source.getAttacker(), LivingEntity.class));

		return true;
	}

	@Override
	public void tick() {
		super.tick();

		if (this.age == 1) this.removeOldDendroCores();

		if (this.type == Type.HYPERBLOOM) this.doHyperbloom();

		if (this.age >= 120 && type != Type.HYPERBLOOM) {
			this.explode(2.0);
			this.remove(RemovalReason.KILLED);
		}
	}

	private void removeOldDendroCores() {
		if (!(this.getWorld() instanceof final ServerWorld world)) return;

		final Box box = Box.of(this.getLerpedPos(1f), DendroCoreEntity.DENDRO_CORES_IN_RADIUS, DendroCoreEntity.DENDRO_CORES_IN_RADIUS, DendroCoreEntity.DENDRO_CORES_IN_RADIUS);
		final List<DendroCoreEntity> dendroCores = this.getWorld().getEntitiesByClass(DendroCoreEntity.class, box, dc -> true);

		if (dendroCores.size() <= 5) return;

		dendroCores.sort(Comparator.comparing(DendroCoreEntity::getAge).reversed());

		final Queue<DendroCoreEntity> queue = new LinkedList<>(dendroCores);

		while (queue.peek() != null && queue.size() > 5) queue.remove().kill(world);
	}

	private boolean explode(final double reactionMultiplier) {
		if (!(this.getWorld() instanceof final ServerWorld world)) return false;

		if (this.exploded) return false;

		this.exploded = true;
		this.age = 117;

		if (!this.getWorld().isClient) this.sendSyncPayload();

		final @Nullable LivingEntity recentOwner = this.getRecentOwner();

		for (final LivingEntity target : ElementalReaction.getEntitiesInAoE(this, 5.0)) {
			if (target instanceof DendroCoreEntity) continue;

			final ElementalDamageSource source = this.createDamageSource(target, recentOwner);

			float damage = ElementalReaction.getReactionDamage(this, reactionMultiplier);

			if (this.owners.contains(target.getUuid())) damage *= 0.02f;

			target.damage(world, source, damage);
		}

		this.getWorld()
			.playSound(null, this.getBlockPos(), SevenElementsSoundEvents.DENDRO_CORE_EXPLOSION, SoundCategory.PLAYERS, 0.5f, 1.0f);

		return true;
	}

	private @Nullable LivingEntity getRecentOwner() {
		return !owners.isEmpty() && this.getWorld() instanceof ServerWorld
			? this.getEntityFromUUID(owners.get(owners.size() - 1))
			: null;
	}

	private ElementalDamageSource createDamageSource(final LivingEntity target) {
		final @Nullable LivingEntity recentOwner = this.getRecentOwner();

		return this.createDamageSource(target, recentOwner);
	}

	private ElementalDamageSource createDamageSource(final LivingEntity target, final LivingEntity recentOwner) {
		return new ElementalDamageSource(
			this.getWorld()
				.getDamageSources()
				.create(SevenElementsDamageTypes.DENDRO_CORE, this, recentOwner),
			ElementalApplications.gaugeUnits(target, Element.DENDRO, 0.0),
			InternalCooldownContext.ofNone(recentOwner)
		).shouldApplyDMGBonus(false);
	}

	private void sendSyncPayload() {
		final SyncDendroCoreS2CPayload packet = new SyncDendroCoreS2CPayload(this);

		for (final ServerPlayerEntity otherPlayer : PlayerLookup.tracking(this))
			ServerPlayNetworking.send(otherPlayer, packet);
	}

	static {
		ElementComponent.denyElementsFor(DendroCoreEntity.class);
	}

	private static enum Type {
		NORMAL, HYPERBLOOM, BURGEON;

		static final Codec<Type> CODEC = Codecs.NON_EMPTY_STRING.xmap(Type::valueOf, Type::toString);
	}

	public static class SyncDendroCoreS2CPayload implements CustomPayload {
		public static final CustomPayload.Id<SyncDendroCoreS2CPayload> ID = new CustomPayload.Id<>(
			SevenElements.identifier("s2c/sync_dendro_core")
		);

		public static final PacketCodec<RegistryByteBuf, SyncDendroCoreS2CPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.INTEGER, inst -> inst.entityId,
			PacketCodecs.INTEGER, inst -> inst.age,
			PacketCodecs.codec(DendroCoreEntity.Type.CODEC), inst -> inst.type,
			SyncDendroCoreS2CPayload::new
		);

		private final int entityId;
		private final int age;
		private final DendroCoreEntity.Type type;

		private SyncDendroCoreS2CPayload(int entityId, int age, DendroCoreEntity.Type type) {
			this.entityId = entityId;
			this.age = age;
			this.type = type;
		}

		private SyncDendroCoreS2CPayload(final DendroCoreEntity dendroCore) {
			this.entityId = dendroCore.getId();
			this.age = dendroCore.getAge();
			this.type = dendroCore.type;
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}

		public int entityId() {
			return this.entityId;
		}
	}
}
