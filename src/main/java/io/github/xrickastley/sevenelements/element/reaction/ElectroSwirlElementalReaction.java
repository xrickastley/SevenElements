package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Colors;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class ElectroSwirlElementalReaction extends AbstractSwirlElementalReaction {
	ElectroSwirlElementalReaction() {
		super(
			new Settings("Swirl", SevenElements.identifier("swirl_electro"), TextHelper.reaction("reaction.seven-elements.swirl", Colors.ANEMO))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.ELECTRO, 4)
				.setTriggeringElement(Element.ANEMO, 1)
				.reversable(true)
		);
	}
}
