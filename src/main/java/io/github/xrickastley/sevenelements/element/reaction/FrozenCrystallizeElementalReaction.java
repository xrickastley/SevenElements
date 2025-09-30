package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class FrozenCrystallizeElementalReaction extends AbstractCrystallizeElementalReaction {
	FrozenCrystallizeElementalReaction() {
		super(
			new Settings("Crystallize", SevenElements.identifier("crystallize_frozen"), TextHelper.reaction("reaction.seven-elements.crystallize", "#f79c00"))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.FREEZE)
				.setTriggeringElement(Element.GEO, 6)
		);
	}
}
