package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.registry.SevenElementsItemTags;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;

public final class HeavyShatterElementalReaction extends AbstractShatterElementalReaction {
	HeavyShatterElementalReaction() {
		super(
			new Settings("Shatter", SevenElements.identifier("shatter_heavy"), TextHelper.reaction("reaction.seven-elements.shatter", "#cfffff"))
				.setReactionCoefficient(0)
				.setAuraElement(Element.FREEZE)
				.setTriggeringElement(Element.PHYSICAL, 0)
		);
	}

	@Override
	public boolean isTriggerable(LivingEntity entity) {
		return ElementComponent.KEY.get(entity).hasElementalApplication(Element.FREEZE)
			&& entity.sevenelements$getPlannedAttacker() != null
			&& entity.sevenelements$getPlannedAttacker() instanceof final LivingEntity attacker
			&& entity.sevenelements$getPlannedDamageSource() != null
			&& entity.sevenelements$getPlannedDamageSource().isDirect()
			&& Registries.ITEM
				.getEntry(attacker.getMainHandStack().getItem())
				.isIn(SevenElementsItemTags.HEAVY_WEAPON);
	}
}
