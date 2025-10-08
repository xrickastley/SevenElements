package io.github.xrickastley.sevenelements.networking;

import java.util.ArrayList;
import java.util.List;

import io.github.xrickastley.sevenelements.SevenElementsClient;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.entity.CrystallizeShardEntity.SyncCrystallizeShardTypeS2CPayload;
import io.github.xrickastley.sevenelements.entity.CrystallizeShardEntity;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.gui.screen.ingame.ElementalInfusionScreen;
import io.github.xrickastley.sevenelements.renderer.WorldTextRenderer.DamageText;
import io.github.xrickastley.sevenelements.renderer.WorldTextRenderer.ReactionText;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.Color;
import io.github.xrickastley.sevenelements.util.Colors;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SevenElementsPacketsS2C {
	private static final List<PayloadHandler<? extends SevenElementsPayload>> HANDLERS = new ArrayList<>();
	private static boolean registered = false;

	public static void register() {
		ClientPlayConnectionEvents.INIT.register(SevenElementsPacketsS2C::onPlayInit);
	}

	public static void registerHandler(final PayloadHandler<? extends SevenElementsPayload> handler) {
		if (registered) throw new IllegalStateException("All ClientPlayConnectionEvents.INIT handlers have already been registered!");

		SevenElementsPacketsS2C.HANDLERS.add(handler);
	}

	private static void registerHandlers() {
		registered = true;

		for (final PayloadHandler<? extends SevenElementsPayload> handler : SevenElementsPacketsS2C.HANDLERS)
			ClientPlayNetworking.registerReceiver(handler.getPayloadId().Type(), ClassInstanceUtil.cast(handler));
	}

	private static void onPlayInit(ClientPlayNetworkHandler handler, MinecraftClient client) {
		ClientPlayNetworking.registerReceiver(ShowElementalReactionS2CPayload.ID.Type(), SevenElementsPacketsS2C::onElementalReactionShow);
		ClientPlayNetworking.registerReceiver(ShowElementalDamageS2CPayload.ID.Type(), SevenElementsPacketsS2C::onElementalDamageShow);
		ClientPlayNetworking.registerReceiver(SyncDendroCoreAgeS2CPayload.ID.Type(), SevenElementsPacketsS2C::onSyncDendroCoreAge);
		ClientPlayNetworking.registerReceiver(SyncCrystallizeShardTypeS2CPayload.ID.Type(), SevenElementsPacketsS2C::onSyncCrystallizeShardElement);
		ClientPlayNetworking.registerReceiver(FinishElementalInfusionS2CPayload.ID.Type(), SevenElementsPacketsS2C::onFinishElementalInfusion);

		SevenElementsPacketsS2C.registerHandlers();
	}

	private static void onElementalReactionShow(ShowElementalReactionS2CPayload payload, ClientPlayerEntity player, PacketSender sender) {
		final Vec3d pos = payload.pos();
		final ElementalReaction reaction = payload.reaction();

		if (reaction == null || reaction.getText() == null) return;

		SevenElementsClient.WORLD_TEXT_RENDERER.addEntry(
			new ReactionText(pos.x, pos.y, pos.z, Colors.PHYSICAL, reaction.getText())
		);
	}

	private static void onElementalDamageShow(ShowElementalDamageS2CPayload payload, ClientPlayerEntity player, PacketSender sender) {
		final ClientConfig config = ClientConfig.get();

		if (!config.rendering.text.showDamageText) return;

		final Vec3d pos = payload.pos();
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

	private static void onSyncDendroCoreAge(SyncDendroCoreAgeS2CPayload payload, ClientPlayerEntity player, PacketSender sender) {
		final World world = MinecraftClient
			.getInstance()
			.player
			.getWorld();

		final Entity entity = world.getEntityById(payload.entityId());

		if (!(entity instanceof final DendroCoreEntity dendroCore)) return;

		dendroCore.age = payload.age();
	}

	private static void onSyncCrystallizeShardElement(SyncCrystallizeShardTypeS2CPayload payload, ClientPlayerEntity player, PacketSender sender) {
		final World world = MinecraftClient
			.getInstance()
			.player
			.getWorld();

		final Entity entity = world.getEntityById(payload.entityId());

		if (!(entity instanceof final CrystallizeShardEntity crystallizeShard)) return;

		crystallizeShard.syncFromPacket(payload);
	}

	private static void onFinishElementalInfusion(FinishElementalInfusionS2CPayload payload, ClientPlayerEntity player, PacketSender sender) {
		final MinecraftClient client = MinecraftClient.getInstance();
		final ScreenHandler screenHandler = player.currentScreenHandler;
		final Screen currentScreen = client.currentScreen;

		if (screenHandler != null
			&& screenHandler.syncId == payload.syncId()
			&& currentScreen instanceof final ElementalInfusionScreen screen
		) {
			screen.finishElementalInfusion(payload);
		}
	}
}
