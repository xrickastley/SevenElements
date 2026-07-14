package io.github.xrickastley.sevenelements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.xrickastley.sevenelements.entity.SevenElementsEntityTypes;
import io.github.xrickastley.sevenelements.gui.screen.ingame.ElementalInfusionScreen;
import io.github.xrickastley.sevenelements.networking.SevenElementsPacketsS2C;
import io.github.xrickastley.sevenelements.networking.SyncBossBarEntityPayloadHandler;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderer;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderers;
import io.github.xrickastley.sevenelements.renderer.entity.CrystallizeShardEntityRenderer;
import io.github.xrickastley.sevenelements.renderer.entity.DendroCoreEntityRenderer;
import io.github.xrickastley.sevenelements.renderer.entity.model.CrystallizeShardEntityModel;
import io.github.xrickastley.sevenelements.renderer.entity.model.DendroCoreEntityModel;
import io.github.xrickastley.sevenelements.screen.SevenElementsScreenHandlers;
import io.github.xrickastley.sevenelements.util.ClientConfig;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.network.chat.Component;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class SevenElementsClient implements ClientModInitializer {
	public static final String MOD_ID = SevenElements.MOD_ID;
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final SyncBossBarEntityPayloadHandler SYNC_BOSS_BAR_ENTITY_HANDLER = new SyncBossBarEntityPayloadHandler();

	@Override
	public void onInitializeClient() {
		SevenElementsClient.LOGGER.info("Seven Elements (Client) Initialized!");

		SevenElementsPacketsS2C.registerHandler(SevenElementsRenderers.CHARGE_AURA_EFFECT);
		SevenElementsPacketsS2C.registerHandler(SevenElementsClient.SYNC_BOSS_BAR_ENTITY_HANDLER);

		LevelExtractionEvents.END_EXTRACTION.register(SevenElementsRenderer::extractAll);
		LevelRenderEvents.END_MAIN.register(SevenElementsRenderer::renderAll);
		ClientTickEvents.START_LEVEL_TICK.register(SevenElementsRenderer::tickAll);

		EntityRenderers.register(SevenElementsEntityTypes.DENDRO_CORE, DendroCoreEntityRenderer::new);
		EntityRenderers.register(SevenElementsEntityTypes.CRYSTALLIZE_SHARD, CrystallizeShardEntityRenderer::new);
		ModelLayerRegistry.registerModelLayer(DendroCoreEntityModel.MODEL_LAYER, DendroCoreEntityModel::getTexturedModelData);
		ModelLayerRegistry.registerModelLayer(CrystallizeShardEntityModel.MODEL_LAYER, CrystallizeShardEntityModel::getTexturedModelData);

		SevenElementsPacketsS2C.register();
		SevenElementsRenderers.register();

		AutoConfig.register(ClientConfig.class, GsonConfigSerializer::new);

		MenuScreens.register(SevenElementsScreenHandlers.ELEMENTAL_INFUSION_SCREEN_HANDLER, ElementalInfusionScreen::new);
	}

	public void overrideSidedImpl() {
		SevenElementsSidedImpl.WRAP_LINES = (text, width) -> {
			final Font textRenderer = Minecraft.getInstance().font;

			return textRenderer.split(text, width)
				.stream()
				.<Component>map(SevenElementsClientUtil.TextRebuilder::rebuild)
				.toList();
		};
	}
}
