package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public abstract sealed class AbstractSuperconductElementalReaction
	extends ElementalReaction
	permits SuperconductElementalReaction, FrozenSuperconductElementalReaction
{
	AbstractSuperconductElementalReaction(Settings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		for (final LivingEntity target  : ElementalReaction.getEntitiesInAoE(entity, 3, t -> t != origin)) {
			final float damage = ElementalReaction.getReactionDamage(entity, 1.5);
			final ElementalDamageSource source = new ElementalDamageSource(
				entity
					.getDamageSources()
					.create(SevenElementsDamageTypes.SUPERCONDUCT, origin),
				ElementalApplications.gaugeUnits(target, Element.CRYO, 0),
				InternalCooldownContext.ofNone(origin)
			).shouldApplyDMGBonus(false);

			target.damage(source, damage);
			target.addStatusEffect(new StatusEffectInstance(SevenElementsStatusEffects.SUPERCONDUCT, 240, 0, true, true), origin);
		}
	}
}
