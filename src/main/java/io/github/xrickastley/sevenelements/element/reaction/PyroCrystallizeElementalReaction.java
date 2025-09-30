package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class PyroCrystallizeElementalReaction extends AbstractCrystallizeElementalReaction {
	PyroCrystallizeElementalReaction() {
		super(
			new Settings("Crystallize", SevenElements.identifier("crystallize_pyro"), TextHelper.reaction("reaction.seven-elements.crystallize", "#f79c00"))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.PYRO)
				.setTriggeringElement(Element.GEO, 3)
		);
	}
}
