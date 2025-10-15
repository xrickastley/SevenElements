package io.github.xrickastley.sevenelements.factory;

import io.github.xrickastley.sevenelements.advancement.criterion.SevenElementsCriteria;
import io.github.xrickastley.sevenelements.block.SevenElementsBlocks;
import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReactions;
import io.github.xrickastley.sevenelements.entity.SevenElementsEntityTypes;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistries;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistryKeys;
import io.github.xrickastley.sevenelements.screen.SevenElementsScreenHandlers;

public class SevenElementsFactories {
	public static void registerAll() {
		SevenElementsRegistryKeys.load();
		SevenElementsRegistries.load();

		SevenElementsAttributes.register();
		SevenElementsBlocks.register();
		SevenElementsComponents.register();
		SevenElementsCriteria.register();
		SevenElementsEntityTypes.register();
		SevenElementsGameRules.register();
		SevenElementsItems.register();
		SevenElementsScreenHandlers.register();
		SevenElementsSoundEvents.register();
		SevenElementsStatusEffects.register();

		ElementalReactions.register();
	}
}
