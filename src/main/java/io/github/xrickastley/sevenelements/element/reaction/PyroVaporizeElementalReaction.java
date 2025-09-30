package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class PyroVaporizeElementalReaction extends AmplifyingElementalReaction {
	PyroVaporizeElementalReaction() {
		super(
			new Settings("Vaporize", SevenElements.identifier("vaporize_pyro"), TextHelper.reaction("reaction.seven-elements.vaporize", "#f2be87"))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.HYDRO)
				.setTriggeringElement(Element.PYRO, 5),
			1.5
		);
	}
}
