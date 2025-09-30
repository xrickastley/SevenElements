package io.github.xrickastley.sevenelements.factory;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.world.GameRules;

public class SevenElementsGameRules {
	public static final GameRules.Key<GameRules.BooleanRule> DO_ELEMENTS = GameRuleRegistry.register(
		"doElements",
		GameRules.Category.MISC,
		GameRuleFactory.createBooleanRule(true)
	);

	public static final GameRules.Key<DoubleRule> LEVEL_MULTIPLIER = GameRuleRegistry.register(
		"levelMultiplier",
		GameRules.Category.MISC,
		GameRuleFactory.createDoubleRule(5)
	);

	public static final GameRules.Key<GameRules.BooleanRule> OVERLOADED_EXPLOSIONS_DAMAGE_BLOCKS = GameRuleRegistry.register(
		"overloadedBlockDestruction",
		GameRules.Category.MISC,
		GameRuleFactory.createBooleanRule(true)
	);

	public static final GameRules.Key<GameRules.BooleanRule> PYRO_FROM_FIRE = GameRuleRegistry.register(
		"pyroFromFire",
		GameRules.Category.MISC,
		GameRuleFactory.createBooleanRule(true)
	);

	public static final GameRules.Key<GameRules.BooleanRule> HYDRO_FROM_WATER = GameRuleRegistry.register(
		"hydroFromWater",
		GameRules.Category.MISC,
		GameRuleFactory.createBooleanRule(true)
	);

	public static final GameRules.Key<GameRules.BooleanRule> ELECTRO_FROM_LIGHTNING = GameRuleRegistry.register(
		"electroFromLightning",
		GameRules.Category.MISC,
		GameRuleFactory.createBooleanRule(true)
	);

	public static final GameRules.Key<GameRules.BooleanRule> INFUSION_TABLE = GameRuleRegistry.register(
		"infusionTable",
		GameRules.Category.MISC,
		GameRuleFactory.createBooleanRule(true)
	);

	public static void register() {}
}
