package io.github.xrickastley.sevenelements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.xrickastley.sevenelements.entity.SevenElementsEntityTypes;
import io.github.xrickastley.sevenelements.events.WorldRenderEnd;
import io.github.xrickastley.sevenelements.gui.screen.ingame.ElementalInfusionScreen;
import io.github.xrickastley.sevenelements.networking.SevenElementsPacketsS2C;
import io.github.xrickastley.sevenelements.networking.SyncBossBarEntityPayloadHandler;
import io.github.xrickastley.sevenelements.renderer.WorldTextRenderer;
import io.github.xrickastley.sevenelements.renderer.entity.CrystallizeShardEntityRenderer;
import io.github.xrickastley.sevenelements.renderer.entity.DendroCoreEntityRenderer;
import io.github.xrickastley.sevenelements.renderer.entity.model.CrystallizeShardEntityModel;
import io.github.xrickastley.sevenelements.renderer.entity.model.DendroCoreEntityModel;
import io.github.xrickastley.sevenelements.renderer.genshin.SpecialEffectsRenderer;
import io.github.xrickastley.sevenelements.screen.SevenElementsScreenHandlers;
import io.github.xrickastley.sevenelements.util.ClientConfig;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.EntityRendererFactories;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class SevenElementsClient implements ClientModInitializer {
	public static final String MOD_ID = SevenElements.MOD_ID;
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final SpecialEffectsRenderer SPECIAL_EFFECTS_RENDERER = new SpecialEffectsRenderer();
	public static final WorldTextRenderer WORLD_TEXT_RENDERER = new WorldTextRenderer();
	public static final SyncBossBarEntityPayloadHandler SYNC_BOSS_BAR_ENTITY_HANDLER = new SyncBossBarEntityPayloadHandler();

	@Override
	public void onInitializeClient() {
		SevenElementsClient.LOGGER.info("Seven Elements (Client) Initialized!");

		SevenElementsPacketsS2C.registerHandler(SevenElementsClient.SPECIAL_EFFECTS_RENDERER);
		SevenElementsPacketsS2C.registerHandler(SevenElementsClient.SYNC_BOSS_BAR_ENTITY_HANDLER);

		WorldRenderEnd.EVENT.register(SevenElementsClient.SPECIAL_EFFECTS_RENDERER::render);
		ClientTickEvents.START_WORLD_TICK.register(SevenElementsClient.SPECIAL_EFFECTS_RENDERER::tick);

		WorldRenderEnd.EVENT.register(SevenElementsClient.WORLD_TEXT_RENDERER::render);
		ClientTickEvents.START_WORLD_TICK.register(SevenElementsClient.WORLD_TEXT_RENDERER::tick);

		EntityRendererFactories.register(SevenElementsEntityTypes.DENDRO_CORE, DendroCoreEntityRenderer::new);
		EntityRendererFactories.register(SevenElementsEntityTypes.CRYSTALLIZE_SHARD, CrystallizeShardEntityRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(DendroCoreEntityModel.MODEL_LAYER, DendroCoreEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(CrystallizeShardEntityModel.MODEL_LAYER, CrystallizeShardEntityModel::getTexturedModelData);

		SevenElementsPacketsS2C.register();

		AutoConfig.register(ClientConfig.class, GsonConfigSerializer::new);

		HandledScreens.register(SevenElementsScreenHandlers.ELEMENTAL_INFUSION_SCREEN_HANDLER, ElementalInfusionScreen::new);
	}

}
