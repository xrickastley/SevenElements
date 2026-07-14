package io.github.xrickastley.sevenelements.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.mixin.client.BossHealthOverlayAccessor;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.entity.LivingEntity;

public class SyncBossBarEntityPayloadHandler implements PayloadHandler<SyncBossBarEntityS2CPayload> {
	private final Map<UUID, LivingEntity> deferredEntities = new HashMap<>();

	@Override
	public Type<SyncBossBarEntityS2CPayload> getPayloadId() {
		return SyncBossBarEntityS2CPayload.ID;
	}

	@Override
	public void receive(SyncBossBarEntityS2CPayload packet, Context context) {
		final Minecraft client = Minecraft.getInstance();
		final Map<UUID, LerpingBossEvent> bossBarMap = ((BossHealthOverlayAccessor) client.gui.hud.getBossOverlay())
			.getEvents();

		final @Nullable LerpingBossEvent bossBar = bossBarMap.get(packet.uuid());
		final @Nullable LivingEntity entity = packet.hasEntity()
			? ClassInstanceUtil.castOrNull(client.level.getEntity(packet.entityId()), LivingEntity.class)
			: null;

		// set to map and call on add action (basically defer)
		if (bossBar == null) {
			if (entity == null) return;

			SevenElements
				.sublogger()
				.warn("Received packet for unknown boss bar! Deferring LivingEntity set for {}", packet.uuid());

			this.deferredEntities.put(packet.uuid(), entity);

			return;
		}

		if (!packet.hasEntity()) {
			bossBar.sevenelements$setEntity(null);

			return;
		}

		if (entity == null) return;

		bossBar.sevenelements$setEntity(entity);
	}

	public LerpingBossEvent setPossibleEntity(LerpingBossEvent bossBar) {
		bossBar.sevenelements$setEntity(this.deferredEntities.get(bossBar.getId()));

		return bossBar;
	}
}
