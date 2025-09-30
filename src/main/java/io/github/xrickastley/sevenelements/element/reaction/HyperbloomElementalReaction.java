package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.util.Colors;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.entity.LivingEntity;

public final class HyperbloomElementalReaction extends AbstractDendroCoreElementalReaction {
	HyperbloomElementalReaction() {
		super(
			new Settings("Hyperbloom", SevenElements.identifier("hyperbloom"), TextHelper.reaction("reaction.seven-elements.hyperbloom", Colors.ELECTRO))
		);
	}

	@Override
	protected void onReaction(DendroCoreEntity dendroCore, @Nullable LivingEntity origin) {
		dendroCore.addOwner(origin).setAsHyperbloom();
	}
}
