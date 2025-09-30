package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class SuperconductElementalReaction extends AbstractSuperconductElementalReaction {
	SuperconductElementalReaction() {
		super(
			new Settings("Superconduct", SevenElements.identifier("superconduct"), TextHelper.reaction("reaction.seven-elements.superconduct", "#bcb0ff"))
				.setReactionCoefficient(1.0)
				.setAuraElement(Element.CRYO, 1)
				.setTriggeringElement(Element.ELECTRO, 6)
				.reversable(true)
				.preventsReactionsAfter(SevenElements.identifier("superconduct_frozen"))
		);
	}
}
