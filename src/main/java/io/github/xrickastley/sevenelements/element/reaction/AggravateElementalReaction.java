package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Colors;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class AggravateElementalReaction extends AdditiveElementalReaction {
	AggravateElementalReaction() {
		super(
			new Settings("Aggravate", SevenElements.identifier("aggravate"), TextHelper.reaction("reaction.seven-elements.aggravate", Colors.ELECTRO))
				.setReactionCoefficient(0)
				.setAuraElement(Element.QUICKEN)
				.setTriggeringElement(Element.ELECTRO, 2)
				.applyResultAsAura(true),
			1.15
		);
	}
}
