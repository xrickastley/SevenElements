package io.github.xrickastley.sevenelements.networking;

import java.util.UUID;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

public record SyncBossBarEntityS2CPayload(UUID uuid, boolean hasEntity, int entityId) implements CustomPayload {
	public static final CustomPayload.Id<SyncBossBarEntityS2CPayload> ID = new CustomPayload.Id<>(
		SevenElements.identifier("s2c/sync_boss_bar_entity")
	);

	public static final PacketCodec<RegistryByteBuf, SyncBossBarEntityS2CPayload> CODEC = PacketCodec.tuple(
		Uuids.PACKET_CODEC, SyncBossBarEntityS2CPayload::uuid,
		PacketCodecs.BOOL, SyncBossBarEntityS2CPayload::hasEntity,
		PacketCodecs.INTEGER, SyncBossBarEntityS2CPayload::entityId,
		SyncBossBarEntityS2CPayload::new
	);

	public SyncBossBarEntityS2CPayload(BossBar bossBar, LivingEntity entity) {
		this(bossBar.getUuid(), entity != null, entity == null ? -1 : entity.getId());
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
