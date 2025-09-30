package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Colors;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class CryoSwirlElementalReaction extends AbstractSwirlElementalReaction {
	CryoSwirlElementalReaction() {
		super(
			new Settings("Swirl", SevenElements.identifier("swirl_cryo"), TextHelper.reaction("reaction.seven-elements.swirl", Colors.ANEMO))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.CRYO, 3)
				.setTriggeringElement(Element.ANEMO, 4)
				.reversable(true)
				.preventsReactionsAfter(SevenElements.identifier("swirl_frozen"))
		);
	}
}
