package io.github.xrickastley.sevenelements;

import com.mojang.brigadier.CommandDispatcher;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.xrickastley.sevenelements.command.BossBarCommand;
import io.github.xrickastley.sevenelements.command.DamageCommand;
import io.github.xrickastley.sevenelements.command.ElementArgumentType;
import io.github.xrickastley.sevenelements.command.ElementCommand;
import io.github.xrickastley.sevenelements.command.InternalCooldownTagType;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.factory.SevenElementsFactories;
import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;
import io.github.xrickastley.sevenelements.registry.SevenElementsPayloadsS2C;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistryKeys;
import io.github.xrickastley.sevenelements.registry.dynamic.DynamicRegistries;
import io.github.xrickastley.sevenelements.registry.dynamic.DynamicRegistryLoadEvents.RegistryContext;
import io.github.xrickastley.sevenelements.registry.dynamic.DynamicRegistryLoadEvents;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class SevenElements implements ModInitializer {
	public static final String MOD_ID = "seven-elements";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Seven Elements Initialized!");

		SevenElementsFactories.registerAll();
		SevenElementsPayloadsS2C.register();

		CommandRegistrationCallback.EVENT.register(SevenElements::onCommandRegistration);
		DynamicRegistryLoadEvents.BEFORE_LOAD.register(SevenElements::onBeforeRegistryLoad);

		DynamicRegistries.registerIdentified(
			InternalCooldownType.class,
			SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE,
			InternalCooldownType.Builder.CODEC,
			InternalCooldownType.CODEC,
			InternalCooldownType.Builder::getInstance
		);

		DynamicRegistries.addUnmodifiableEntries(
			SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE,
			InternalCooldownType.getPreloadedInstances()
		);

		ArgumentTypeRegistry.registerArgumentType(
			SevenElements.identifier("element"),
			ElementArgumentType.class,
			ConstantArgumentSerializer.of(ElementArgumentType::new)
		);

		ArgumentTypeRegistry.registerArgumentType(
			SevenElements.identifier("internal_cooldown_tag"),
			InternalCooldownTagType.class,
			ConstantArgumentSerializer.of(InternalCooldownTagType::new)
		);

	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static Logger sublogger() {
		final String className = Thread.currentThread().getStackTrace()[2].getClassName();

		return SevenElements.sublogger(className.substring(className.lastIndexOf(".") + 1));
	}

	public static Logger sublogger(String sublogger) {
		return LoggerFactory.getLogger(MOD_ID + "/" + sublogger);
	}

	public static Logger sublogger(Class<?> sublogger) {
		return LoggerFactory.getLogger(MOD_ID + "/" + sublogger.getSimpleName());
	}

	public static Logger sublogger(Object sublogger) {
		return LoggerFactory.getLogger(MOD_ID + "/" + sublogger.getClass().getSimpleName());
	}

	public static float getLevelMultiplier(Entity entity) {
		return getLevelMultiplier(entity.getWorld());
	}

	public static float getLevelMultiplier(World world) {
		return (float) world
			.getGameRules()
			.get(SevenElementsGameRules.LEVEL_MULTIPLIER)
			.get();
	}

	private static void onCommandRegistration(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
		BossBarCommand.register(dispatcher);
		ElementCommand.register(dispatcher, registryAccess);
		DamageCommand.register(dispatcher, registryAccess);
	}

	private static void onBeforeRegistryLoad(RegistryContext<?> registryContext) {
		final @Nullable RegistryContext<InternalCooldownType> context = registryContext.asKey(SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE);

		if (context == null) return;

		final Registry<InternalCooldownType> registry = context.registry();

		if (registry.containsId(InternalCooldownType.DEFAULT.getId())) return;

		InternalCooldownType.onBeforeRegistryLoad(registry);
	}
}
