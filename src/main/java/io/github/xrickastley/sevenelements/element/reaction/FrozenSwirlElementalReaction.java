package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Colors;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class FrozenSwirlElementalReaction extends AbstractSwirlElementalReaction {
	FrozenSwirlElementalReaction() {
		super(
			new Settings("Swirl", SevenElements.identifier("swirl_frozen"), TextHelper.reaction("reaction.seven-elements.swirl", Colors.ANEMO))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.FREEZE, 4)
				.setTriggeringElement(Element.ANEMO, 5)
				.reversable(true),
			Element.CRYO
		);
	}
}
