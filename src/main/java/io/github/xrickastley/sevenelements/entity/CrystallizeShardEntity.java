package io.github.xrickastley.sevenelements.entity;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.registry.SevenElementsEntityTypeTags;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;
import io.github.xrickastley.sevenelements.util.MathHelper2;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

// Should technically extend Entity, but extends LivingEntity instead to NOT deal with more Networking and Spawn Packets.
public final class CrystallizeShardEntity extends SevenElementsEntity {
	public final AnimationState idleAnimationState = new AnimationState();
	private @Nullable Element element;
	private @Nullable UUID owner;

	CrystallizeShardEntity(EntityType<? extends LivingEntity> entityType, World world) {
		this(entityType, world, null, null);
	}

	public CrystallizeShardEntity(EntityType<? extends LivingEntity> entityType, World world, Element element) {
		this(entityType, world, element, null);
	}

	public CrystallizeShardEntity(EntityType<? extends LivingEntity> entityType, World world, Element element, @Nullable LivingEntity owner) {
		super(entityType, world);

		this.element = this.getWorld().isClient ? null : JavaScriptUtil.nullishCoalesing(element, Element.GEO);
		this.owner = ClassInstanceUtil.mapOrNull(owner, LivingEntity::getUuid);
	}

	public static CrystallizeShardEntity create(ServerWorld world, @Nullable LivingEntity owner, Element element, Vec3d pos, SpawnReason reason) {
		return SevenElementsEntityTypes.CRYSTALLIZE_SHARD.create(
			world,
			shard -> {
				shard.element = element;
				shard.owner = ClassInstanceUtil.mapOrNull(owner, LivingEntity::getUuid);
			},
			MathHelper2.asBlockPos(pos),
			reason,
			true,
			false
		);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);

		if (this.element != null) nbt.putString("Element", this.element.toString());

		if (this.owner != null) nbt.putUuid("Owner", this.owner);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);

		if (nbt.contains("Element")) this.element = Element.valueOf(nbt.getString("Element"));

		if (nbt.contains("Owner")) this.owner = nbt.getUuid("Owner");
	}

	@Override
	public void tick() {
		super.tick();

		this.idleAnimationState.startIfNotRunning(this.age);

		this.checkCrystallizeShield();
		this.syncToPlayers();
	}

	@Override
	public boolean collidesWith(Entity other) {
		return other instanceof CrystallizeShardEntity;
	}

	/**
	 * Gets the element of this {@code CrystallizeShardEntity}. <br> <br>
	 *
	 * This is guaranteed to only be nullable <b>if</b> the world is on the client, as the element
	 * is considered {@code null} until the sync packet is received from the server. <br> <br>
	 *
	 * While the element is considered {@code null}, the Crystallize Shard is not rendered. <br> <br>
	 */
	public @Nullable Element getElement() {
		return this.getWorld().isClient
			? element
			: JavaScriptUtil.nullishCoalesing(element, Element.GEO);
	}

	public void syncFromPacket(SyncCrystallizeShardTypeS2CPayload packet) {
		this.element = packet.element;
	}

	public void syncToPlayers() {
		if (!(this.getWorld() instanceof ServerWorld)) return;

		final SyncCrystallizeShardTypeS2CPayload packet = new SyncCrystallizeShardTypeS2CPayload(this.getId(), this.element);

		for (final ServerPlayerEntity otherPlayer : PlayerLookup.tracking(this))
			ServerPlayNetworking.send(otherPlayer, packet);
	}

	private void checkCrystallizeShield() {
		if (this.getWorld().isClient) return;

		final List<LivingEntity> entities = ElementalReaction.getEntitiesInAoE(this, 1.0, e -> !(e instanceof SevenElementsEntity || e.getType().isIn(SevenElementsEntityTypeTags.IGNORED_TARGETS)));
		final @Nullable LivingEntity owner = this.getEntityFromUUID(this.owner);

		@Nullable LivingEntity target = null;

		if (this.age > 300) {
			this.remove(RemovalReason.KILLED);
		} else if (this.age <= 150 && entities.contains(owner)) {
			target = owner;
		} else if (this.owner == null || this.age > 150) {
			target = entities
				.stream()
				.min(Comparator.comparingDouble(this::distanceTo))
				.orElse(null);
		}

		if (target == null) return;

		final ElementComponent component = ElementComponent.KEY.get(target);

		component.setCrystallizeShield(element, SevenElements.getLevelMultiplier(this));

		this.getWorld()
			.playSound(null, this.getBlockPos(), SevenElementsSoundEvents.CRYSTALLIZE_SHIELD, SoundCategory.PLAYERS, 1.0f, 1.0f);

		this.remove(RemovalReason.KILLED);
	}

	static {
		ElementComponent.denyElementsFor(CrystallizeShardEntity.class);
	}

	public static class SyncCrystallizeShardTypeS2CPayload implements CustomPayload {
		public static final CustomPayload.Id<SyncCrystallizeShardTypeS2CPayload> ID = new CustomPayload.Id<>(
			SevenElements.identifier("s2c/sync_crystallize_shard_type")
		);

		public static final PacketCodec<RegistryByteBuf, SyncCrystallizeShardTypeS2CPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.INTEGER, SyncCrystallizeShardTypeS2CPayload::entityId,
			PacketCodecs.codec(Element.CODEC), inst -> inst.element,
			SyncCrystallizeShardTypeS2CPayload::new
		);

		private final int entityId;
		private final Element element;

		private SyncCrystallizeShardTypeS2CPayload(int entityId, Element element) {
			this.entityId = entityId;
			this.element = element;
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}

		public int entityId() {
			return entityId;
		}
	}
}
