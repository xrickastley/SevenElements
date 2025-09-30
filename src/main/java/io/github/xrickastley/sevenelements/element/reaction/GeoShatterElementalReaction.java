package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class GeoShatterElementalReaction extends AbstractShatterElementalReaction {
	GeoShatterElementalReaction() {
		super(
			new Settings("Shatter", SevenElements.identifier("shatter_geo"), TextHelper.reaction("reaction.seven-elements.shatter", "#cfffff"))
				.setReactionCoefficient(0)
				.setAuraElement(Element.FREEZE)
				.setTriggeringElement(Element.GEO, 1)
		);
	}
}
