package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Colors;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class HydroSwirlElementalReaction extends AbstractSwirlElementalReaction {
	HydroSwirlElementalReaction() {
		super(
			new Settings("Swirl", SevenElements.identifier("swirl_hydro"), TextHelper.reaction("reaction.seven-elements.swirl", Colors.ANEMO))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.HYDRO, 2)
				.setTriggeringElement(Element.ANEMO, 3)
				.reversable(true),
			true
		);
	}
}
