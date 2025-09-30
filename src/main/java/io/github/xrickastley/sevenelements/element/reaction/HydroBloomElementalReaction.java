package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class HydroBloomElementalReaction extends AbstractBloomElementalReaction {
	HydroBloomElementalReaction() {
		super(
			new Settings("Bloom", SevenElements.identifier("bloom_hydro"), TextHelper.reaction("reaction.seven-elements.bloom", "#01e858"))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.DENDRO)
				.setTriggeringElement(Element.HYDRO, 4)
				.preventsReactionsAfter(SevenElements.identifier("spread"), SevenElements.identifier("bloom_quicken"))
		);
	}
}
