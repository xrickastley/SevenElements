package io.github.xrickastley.sevenelements.factory;

import io.github.xrickastley.sevenelements.SevenElements;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;

public class SevenElementsGameRules {
	private static final GameRuleCategory ELEMENTS_CATEGORY = GameRuleCategory.register(SevenElements.identifier("elements"));

	public static final GameRule<Boolean> DO_ELEMENTS
		 = GameRuleBuilder
			.forBoolean(true)
			.category(ELEMENTS_CATEGORY)
			.buildAndRegister(SevenElements.identifier("do_elements"));

	public static final GameRule<Double> LEVEL_MULTIPLIER
		 = GameRuleBuilder
			.forDouble(5)
			.category(ELEMENTS_CATEGORY)
			.buildAndRegister(SevenElements.identifier("level_multiplier"));

	public static final GameRule<Boolean> OVERLOADED_EXPLOSIONS_DAMAGE_BLOCKS
		 = GameRuleBuilder
			.forBoolean(false)
			.category(ELEMENTS_CATEGORY)
			.buildAndRegister(SevenElements.identifier("overloaded_block_destruction"));

	public static final GameRule<Boolean> OVERLOADED_EXPLOSIONS_CREATE_FIRE
		 = GameRuleBuilder
			.forBoolean(true)
			.category(ELEMENTS_CATEGORY)
			.buildAndRegister(SevenElements.identifier("overloaded_creates_fire"));

	public static final GameRule<Boolean> PYRO_FROM_FIRE
		 = GameRuleBuilder
			.forBoolean(true)
			.category(ELEMENTS_CATEGORY)
			.buildAndRegister(SevenElements.identifier("pyro_from_fire"));

	public static final GameRule<Boolean> HYDRO_FROM_WATER
		 = GameRuleBuilder
			.forBoolean(true)
			.category(ELEMENTS_CATEGORY)
			.buildAndRegister(SevenElements.identifier("hydro_from_water"));

	public static final GameRule<Boolean> ELECTRO_FROM_LIGHTNING
		 = GameRuleBuilder
			.forBoolean(true)
			.category(ELEMENTS_CATEGORY)
			.buildAndRegister(SevenElements.identifier("electro_from_lightning"));

	public static final GameRule<Boolean> INFUSION_TABLE
		 = GameRuleBuilder
			.forBoolean(true)
			.category(ELEMENTS_CATEGORY)
			.buildAndRegister(SevenElements.identifier("infusion_table"));

	public static void register() {}
}
