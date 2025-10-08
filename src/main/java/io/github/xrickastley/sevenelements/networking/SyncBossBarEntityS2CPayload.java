package io.github.xrickastley.sevenelements.networking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.UUID;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.Uuids;

public record SyncBossBarEntityS2CPayload(UUID uuid, boolean hasEntity, int entityId) implements SevenElementsPayload {
	public static final Codec<SyncBossBarEntityS2CPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Uuids.CODEC.fieldOf("uuid").forGetter(SyncBossBarEntityS2CPayload::uuid),
		Codec.BOOL.fieldOf("hasEntity").forGetter(SyncBossBarEntityS2CPayload::hasEntity),
		Codec.INT.fieldOf("entityId").forGetter(SyncBossBarEntityS2CPayload::entityId)
	).apply(instance, SyncBossBarEntityS2CPayload::new));

	public static final SevenElementsPayload.Id<SyncBossBarEntityS2CPayload> ID = new SevenElementsPayload.Id<>(
		SevenElements.identifier("s2c/sync_boss_bar_entity"),
		SyncBossBarEntityS2CPayload.CODEC
	);

	public SyncBossBarEntityS2CPayload(BossBar bossBar, LivingEntity entity) {
		this(bossBar.getUuid(), entity != null, entity == null ? -1 : entity.getId());
	}

	@Override
	public Id<? extends SevenElementsPayload> getId() {
		return ID;
	}

	@Override
	public Codec<? extends SevenElementsPayload> getCodec() {
		return CODEC;
	}
}
