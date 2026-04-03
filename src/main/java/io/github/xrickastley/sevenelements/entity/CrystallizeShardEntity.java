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
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

// Should technically extend Entity, but extends LivingEntity instead to NOT deal with more Networking and Spawn Packets.
public final class CrystallizeShardEntity extends SevenElementsEntity {
	public final AnimationState idleAnimationState = new AnimationState();
	private @Nullable Element element;
	private @Nullable UUID owner;

	CrystallizeShardEntity(EntityType<? extends LivingEntity> entityType, Level world) {
		this(entityType, world, null, null);
	}

	public CrystallizeShardEntity(EntityType<? extends LivingEntity> entityType, Level world, Element element) {
		this(entityType, world, element, null);
	}

	public CrystallizeShardEntity(EntityType<? extends LivingEntity> entityType, Level world, Element element, @Nullable LivingEntity owner) {
		super(entityType, world);

		this.element = this.level().isClientSide() ? null : JavaScriptUtil.nullishCoalesing(element, Element.GEO);
		this.owner = ClassInstanceUtil.mapOrNull(owner, LivingEntity::getUUID);
	}

	public static CrystallizeShardEntity create(ServerLevel world, @Nullable LivingEntity owner, Element element, Vec3 pos, EntitySpawnReason reason) {
		return SevenElementsEntityTypes.CRYSTALLIZE_SHARD.create(
			world,
			shard -> {
				shard.element = element;
				shard.owner = ClassInstanceUtil.mapOrNull(owner, LivingEntity::getUUID);
			},
			MathHelper2.asBlockPos(pos),
			reason,
			true,
			false
		);
	}

	@Override
	public void addAdditionalSaveData(ValueOutput view) {
		super.addAdditionalSaveData(view);

		view.store("Element", Element.CODEC, this.element);
		view.storeNullable("Owner", UUIDUtil.AUTHLIB_CODEC, this.owner);
	}

	@Override
	public void readAdditionalSaveData(ValueInput view) {
		super.readAdditionalSaveData(view);

		this.element = view.read("Element", Element.CODEC).orElse(this.element);
		this.owner = view.read("Owner", UUIDUtil.AUTHLIB_CODEC).orElse(this.owner);
	}

	@Override
	public void tick() {
		super.tick();

		this.idleAnimationState.startIfStopped(this.tickCount);

		this.checkCrystallizeShield();
		this.syncToPlayers();
	}

	@Override
	public boolean canCollideWith(Entity other) {
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
		return element;
	}

	public void syncFromPacket(SyncCrystallizeShardTypeS2CPayload packet) {
		this.element = packet.element;
	}

	public void syncToPlayers() {
		if (!(this.level() instanceof ServerLevel)) return;

		final SyncCrystallizeShardTypeS2CPayload packet = new SyncCrystallizeShardTypeS2CPayload(this.getId(), this.element);

		for (final ServerPlayer otherPlayer : PlayerLookup.tracking(this))
			ServerPlayNetworking.send(otherPlayer, packet);
	}

	private void checkCrystallizeShield() {
		if (this.level().isClientSide()) return;

		final List<LivingEntity> entities = ElementalReaction.getEntitiesInAoE(this, 1.0, e -> !(e instanceof SevenElementsEntity || e.is(SevenElementsEntityTypeTags.IGNORED_TARGETS)));
		final @Nullable LivingEntity owner = this.getEntityFromUUID(this.owner);

		@Nullable LivingEntity target = null;

		if (this.tickCount > 300) {
			this.remove(RemovalReason.KILLED);
		} else if (this.tickCount <= 150 && entities.contains(owner)) {
			target = owner;
		} else if (this.owner == null || this.tickCount > 150) {
			target = entities
				.stream()
				.min(Comparator.comparingDouble(this::distanceTo))
				.orElse(null);
		}

		if (target == null) return;

		final ElementComponent component = ElementComponent.KEY.get(target);

		component.setCrystallizeShield(element, SevenElements.getLevelMultiplier(this));

		this.level()
			.playSound(null, this.blockPosition(), SevenElementsSoundEvents.CRYSTALLIZE_SHIELD, SoundSource.PLAYERS, 1.0f, 1.0f);

		this.remove(RemovalReason.KILLED);
	}

	static {
		ElementComponent.denyElementsFor(CrystallizeShardEntity.class);
	}

	public static class SyncCrystallizeShardTypeS2CPayload implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<SyncCrystallizeShardTypeS2CPayload> ID = new CustomPacketPayload.Type<>(
			SevenElements.identifier("s2c/sync_crystallize_shard_type")
		);

		public static final StreamCodec<RegistryFriendlyByteBuf, SyncCrystallizeShardTypeS2CPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, SyncCrystallizeShardTypeS2CPayload::entityId,
			ByteBufCodecs.fromCodec(Element.CODEC), inst -> inst.element,
			SyncCrystallizeShardTypeS2CPayload::new
		);

		private final int entityId;
		private final Element element;

		private SyncCrystallizeShardTypeS2CPayload(int entityId, Element element) {
			this.entityId = entityId;
			this.element = element;
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}

		public int entityId() {
			return entityId;
		}
	}
}
