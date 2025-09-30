package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.util.Colors;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.entity.LivingEntity;

public final class BurgeonElementalReaction extends AbstractDendroCoreElementalReaction {
	BurgeonElementalReaction() {
		super(
			new Settings("Burgeon", SevenElements.identifier("burgeon"), TextHelper.reaction("reaction.seven-elements.burgeon", Colors.PYRO))
		);
	}

	@Override
	protected void onReaction(DendroCoreEntity dendroCore, @Nullable LivingEntity origin) {
		dendroCore.addOwner(origin).setAsBurgeon();
	}
}
