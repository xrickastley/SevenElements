package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;

import net.minecraft.entity.LivingEntity;

public final class PyroFrozenMeltElementalReaction extends AbstractPyroMeltElementalReaction {
	PyroFrozenMeltElementalReaction() {
		super("Melt", "melt_pyro-frozen", Element.FREEZE);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		super.onReaction(entity, auraElement, triggeringElement, reducedGauge, origin);

		// The Aura element is always Freeze since "Freeze" Melt is a non-reversable Forward reaction.
		// Remove frozen effect upon all depletion of gauge units.
		if (auraElement.getGaugeUnits() <= 0.0)
			entity.removeStatusEffect(SevenElementsStatusEffects.FROZEN);
	}
}
