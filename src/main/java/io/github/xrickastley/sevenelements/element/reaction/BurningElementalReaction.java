package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Colors;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class BurningElementalReaction extends AbstractBurningElementalReaction {
	BurningElementalReaction() {
		super(
			new Settings("Burning", SevenElements.identifier("burning"), TextHelper.reaction("reaction.seven-elements.burning", Colors.PYRO))
				.setReactionCoefficient(0) // Coefficient: 0 since Burning is "special", removes itself when Dendro is gone/by natural causes.
				.setAuraElement(Element.DENDRO, 3)
				.setTriggeringElement(Element.PYRO, 6)
				.applyResultAsAura(true)
				.reversable(true)
		);
	}
}
