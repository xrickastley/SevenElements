package io.github.xrickastley.sevenelements.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.mixin.client.BossBarHudAccessor;
import io.github.xrickastley.sevenelements.networking.SevenElementsPayload.Id;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;

public class SyncBossBarEntityPayloadHandler implements PayloadHandler<SyncBossBarEntityS2CPayload> {
	private final Map<UUID, LivingEntity> deferredEntities = new HashMap<>();

	@Override
	public Id<SyncBossBarEntityS2CPayload> getPayloadId() {
		return SyncBossBarEntityS2CPayload.ID;
	}

	@Override
	public void receive(SyncBossBarEntityS2CPayload packet, ClientPlayerEntity player, PacketSender sender) {
		final MinecraftClient client = MinecraftClient.getInstance();
		final Map<UUID, ClientBossBar> bossBarMap = ((BossBarHudAccessor) client.inGameHud.getBossBarHud())
			.getBossBars();

		final @Nullable ClientBossBar bossBar = bossBarMap.get(packet.uuid());
		final @Nullable LivingEntity entity = packet.hasEntity()
			? ClassInstanceUtil.castOrNull(client.world.getEntityById(packet.entityId()), LivingEntity.class)
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

	public ClientBossBar setPossibleEntity(ClientBossBar bossBar) {
		bossBar.sevenelements$setEntity(this.deferredEntities.get(bossBar.getUuid()));

		return bossBar;
	}
}
