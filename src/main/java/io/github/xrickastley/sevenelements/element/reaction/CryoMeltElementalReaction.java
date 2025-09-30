package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class CryoMeltElementalReaction extends AmplifyingElementalReaction {
	CryoMeltElementalReaction() {
		super(
			new Settings("Melt", SevenElements.identifier("melt_cryo"), TextHelper.reaction("reaction.seven-elements.melt", "#f2be87"))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.PYRO)
				.setTriggeringElement(Element.CRYO, 2),
			1.5
		);
	}
}
