package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class HydroVaporizeElementalReaction extends AmplifyingElementalReaction {
	HydroVaporizeElementalReaction() {
		super(
			new Settings("Vaporize", SevenElements.identifier("vaporize_hydro"), TextHelper.reaction("reaction.seven-elements.vaporize", "#f2be87"))
				.setReactionCoefficient(2.0)
				.setAuraElement(Element.PYRO)
				.setTriggeringElement(Element.HYDRO, 1),
			2
		);
	}
}
