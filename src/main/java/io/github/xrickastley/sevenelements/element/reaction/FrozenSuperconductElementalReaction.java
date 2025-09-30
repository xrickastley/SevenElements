package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class FrozenSuperconductElementalReaction extends AbstractSuperconductElementalReaction {
	FrozenSuperconductElementalReaction() {
		super(
			new Settings("Superconduct", SevenElements.identifier("superconduct_frozen"), TextHelper.reaction("reaction.seven-elements.superconduct", "#bcb0ff"))
				.setReactionCoefficient(1.0)
				.setAuraElement(Element.FREEZE, 2)
				.setTriggeringElement(Element.ELECTRO, 7)
				.reversable(true)
				.preventsReactionsAfter()
		);
	}
}
