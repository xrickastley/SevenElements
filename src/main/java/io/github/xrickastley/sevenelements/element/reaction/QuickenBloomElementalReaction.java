package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class QuickenBloomElementalReaction extends AbstractBloomElementalReaction {
	QuickenBloomElementalReaction() {
		super(
			new Settings("Bloom", SevenElements.identifier("bloom_quicken"), TextHelper.reaction("reaction.seven-elements.bloom", "#01e858"))
				.setReactionCoefficient(2.0)
				.setAuraElement(Element.QUICKEN)
				.setTriggeringElement(Element.HYDRO, 4)
		);
	}
}
