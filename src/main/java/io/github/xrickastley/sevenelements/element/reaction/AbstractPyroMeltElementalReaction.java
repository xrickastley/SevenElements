package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

public abstract sealed class AbstractPyroMeltElementalReaction
	extends AmplifyingElementalReaction
	permits PyroCryoMeltElementalReaction, PyroFrozenMeltElementalReaction
{
	AbstractPyroMeltElementalReaction(String name, String idPath, Element auraElement) {
		super(
			new Settings(name, SevenElements.identifier(idPath), TextHelper.reaction("reaction.seven-elements.melt", "#f2be87"))
				.setReactionCoefficient(2.0)
				.setAuraElement(auraElement)
				.setTriggeringElement(Element.PYRO, 5)
				.preventsReactionsAfter(SevenElements.identifier("vaporize_pyro"), SevenElements.identifier("melt_pyro-frozen")),
			2
		);
	}
}
