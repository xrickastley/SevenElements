package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Colors;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class PyroSwirlElementalReaction extends AbstractSwirlElementalReaction {
	PyroSwirlElementalReaction() {
		super(
			new Settings("Swirl", SevenElements.identifier("swirl_pyro"), TextHelper.reaction("reaction.seven-elements.swirl", Colors.ANEMO))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.PYRO, 3)
				.setTriggeringElement(Element.ANEMO, 2)
				.reversable(true)
		);
	}
}
