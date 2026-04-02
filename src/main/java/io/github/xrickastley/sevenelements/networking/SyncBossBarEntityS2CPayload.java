package io.github.xrickastley.sevenelements.networking;

import java.util.UUID;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;

public record SyncBossBarEntityS2CPayload(UUID uuid, boolean hasEntity, int entityId) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SyncBossBarEntityS2CPayload> ID = new CustomPacketPayload.Type<>(
		SevenElements.identifier("s2c/sync_boss_bar_entity")
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncBossBarEntityS2CPayload> CODEC = StreamCodec.composite(
		UUIDUtil.STREAM_CODEC, SyncBossBarEntityS2CPayload::uuid,
		ByteBufCodecs.BOOL, SyncBossBarEntityS2CPayload::hasEntity,
		ByteBufCodecs.INT, SyncBossBarEntityS2CPayload::entityId,
		SyncBossBarEntityS2CPayload::new
	);

	public SyncBossBarEntityS2CPayload(BossEvent bossBar, LivingEntity entity) {
		this(bossBar.getId(), entity != null, entity == null ? -1 : entity.getId());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
