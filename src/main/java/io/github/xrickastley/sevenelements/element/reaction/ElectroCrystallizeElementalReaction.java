package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class ElectroCrystallizeElementalReaction extends AbstractCrystallizeElementalReaction {
	ElectroCrystallizeElementalReaction() {
		super(
			new Settings("Crystallize", SevenElements.identifier("crystallize_electro"), TextHelper.reaction("reaction.seven-elements.crystallize", "#f79c00"))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.ELECTRO)
				.setTriggeringElement(Element.GEO, 2)
		);
	}
}
