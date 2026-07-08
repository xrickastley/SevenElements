package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.reaction.base.AdditiveElementalReaction;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class SpreadElementalReaction extends AdditiveElementalReaction {
	SpreadElementalReaction() {
		super(
			new Settings("Spread", SevenElements.identifier("spread"), TextHelper.reaction("reaction.seven-elements.spread", "#01e858"))
				.setReactionCoefficient(0)
				.setReactionMultiplier(1.25)
				.setAuraElement(Element.QUICKEN)
				.setTriggeringElement(Element.DENDRO, 1)
				.applyResultAsAura(true)
		);
	}
}
