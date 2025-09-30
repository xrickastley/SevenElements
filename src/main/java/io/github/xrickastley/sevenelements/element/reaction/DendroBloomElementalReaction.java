package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

public final class DendroBloomElementalReaction extends AbstractBloomElementalReaction {
	DendroBloomElementalReaction() {
		super(
			new Settings("Bloom", SevenElements.identifier("bloom_dendro"), TextHelper.reaction("reaction.seven-elements.bloom", "#01e858"))
				.setReactionCoefficient(2)
				.setAuraElement(Element.HYDRO)
				.setTriggeringElement(Element.DENDRO, 4)
		);
	}
}
