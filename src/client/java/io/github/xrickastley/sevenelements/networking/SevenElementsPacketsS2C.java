package io.github.xrickastley.sevenelements.networking;

import java.util.ArrayList;
import java.util.List;

import io.github.xrickastley.sevenelements.SevenElementsClient;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.entity.CrystallizeShardEntity.SyncCrystallizeShardTypeS2CPayload;
import io.github.xrickastley.sevenelements.entity.CrystallizeShardEntity;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity.SyncDendroCoreStateS2CPayload;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.gui.screen.ingame.ElementalInfusionScreen;
import io.github.xrickastley.sevenelements.renderer.WorldTextRenderer.DamageText;
import io.github.xrickastley.sevenelements.renderer.WorldTextRenderer.ReactionText;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.Color;
import io.github.xrickastley.sevenelements.util.Colors;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SevenElementsPacketsS2C {
	private static final List<PayloadHandler<? extends CustomPacketPayload>> HANDLERS = new ArrayList<>();
	private static boolean registered = false;

	public static void register() {
		ClientPlayConnectionEvents.INIT.register(SevenElementsPacketsS2C::onPlayInit);
	}

	public static void registerHandler(final PayloadHandler<? extends CustomPacketPayload> handler) {
		if (registered) throw new IllegalStateException("All ClientPlayConnectionEvents.INIT handlers have already been registered!");

		SevenElementsPacketsS2C.HANDLERS.add(handler);
	}

	private static void registerHandlers() {
		registered = true;

		for (final PayloadHandler<? extends CustomPacketPayload> handler : SevenElementsPacketsS2C.HANDLERS)
			ClientPlayNetworking.registerGlobalReceiver(handler.getPayloadId(), ClassInstanceUtil.cast(handler));
	}

	private static void onPlayInit(ClientPacketListener handler, Minecraft client) {
		ClientPlayNetworking.registerGlobalReceiver(ShowElementalReactionS2CPayload.ID, SevenElementsPacketsS2C::onElementalReactionShow);
		ClientPlayNetworking.registerGlobalReceiver(ShowElementalDamageS2CPayload.ID, SevenElementsPacketsS2C::onElementalDamageShow);
		ClientPlayNetworking.registerGlobalReceiver(SyncDendroCoreStateS2CPayload.ID, SevenElementsPacketsS2C::onSyncDendroCoreState);
		ClientPlayNetworking.registerGlobalReceiver(SyncCrystallizeShardTypeS2CPayload.ID, SevenElementsPacketsS2C::onSyncCrystallizeShardElement);
		ClientPlayNetworking.registerGlobalReceiver(FinishElementalInfusionS2CPayload.ID, SevenElementsPacketsS2C::onFinishElementalInfusion);

		SevenElementsPacketsS2C.registerHandlers();
	}

	private static void onElementalReactionShow(ShowElementalReactionS2CPayload payload, Context context) {
		final Vec3 pos = payload.pos();
		final ElementalReaction reaction = payload.reaction();

		if (reaction == null || reaction.getText() == null) return;

		SevenElementsClient.WORLD_TEXT_RENDERER.addEntry(
			new ReactionText(pos.x, pos.y, pos.z, Colors.PHYSICAL, reaction.getText())
		);
	}

	private static void onElementalDamageShow(ShowElementalDamageS2CPayload payload, Context context) {
		final ClientConfig config = ClientConfig.get();

		if (!config.rendering.text.showDamageText) return;

		final Vec3 pos = payload.pos();
		final Color color = payload.element() != null && payload.element().hasDamageColor()
			? payload.element().getDamageColor()
			: Colors.PHYSICAL;
		final float amount = config.developer.genshinDamageLim
			? Math.min(payload.amount(), 20_000_000)
			: payload.amount();

		if (amount == Float.MAX_VALUE) return;

		SevenElementsClient.WORLD_TEXT_RENDERER.addEntry(
			new DamageText(pos.x, pos.y, pos.z, color, amount, payload.crit() ? config.rendering.text.critDMGScale : config.rendering.text.normalDMGScale)
		);
	}

	private static void onSyncDendroCoreState(SyncDendroCoreStateS2CPayload payload, Context context) {
		final Level world = Minecraft
			.getInstance()
			.player
			.level();

		final Entity entity = world.getEntity(payload.entityId());

		if (!(entity instanceof final DendroCoreEntity dendroCore)) return;

		dendroCore.syncFromPacket(payload);
	}

	private static void onSyncCrystallizeShardElement(SyncCrystallizeShardTypeS2CPayload payload, Context context) {
		final Level world = Minecraft
			.getInstance()
			.player
			.level();

		final Entity entity = world.getEntity(payload.entityId());

		if (!(entity instanceof final CrystallizeShardEntity crystallizeShard)) return;

		crystallizeShard.syncFromPacket(payload);
	}

	private static void onFinishElementalInfusion(FinishElementalInfusionS2CPayload payload, Context context) {
		final Player playerEntity = context.player();
		final AbstractContainerMenu screenHandler = playerEntity.containerMenu;
		final Screen currentScreen = context.client().screen;

		if (screenHandler != null
			&& screenHandler.containerId == payload.syncId()
			&& currentScreen instanceof final ElementalInfusionScreen screen
		) {
			screen.finishElementalInfusion(payload);
		}
	}
}
