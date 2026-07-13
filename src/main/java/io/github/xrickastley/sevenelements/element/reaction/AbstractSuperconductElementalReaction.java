package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public abstract sealed class AbstractSuperconductElementalReaction
	extends ElementalReaction
	permits SuperconductElementalReaction, FrozenSuperconductElementalReaction
{
	AbstractSuperconductElementalReaction(Settings settings) {
		super(
			settings
				.setReactionMultiplier(1.5)
		);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		if (!(entity.level() instanceof final ServerLevel world)) return;

		for (final LivingEntity target : ElementalReaction.getEntitiesInAoE(entity, 3, t -> t != origin)) {
			final float damage = this.getReactionStrength(entity, world);
			final ElementalDamageSource source = new ElementalDamageSource(
				entity
					.damageSources()
					.source(SevenElementsDamageTypes.SUPERCONDUCT, origin),
				ElementalApplications.gaugeUnits(target, Element.CRYO, 0),
				InternalCooldownContext.ofNone(origin)
			).shouldApplyDMGBonus(false);

			target.hurtServer(world, source, damage);
			target.addEffect(new MobEffectInstance(SevenElementsStatusEffects.SUPERCONDUCT, 240, 0, false, true), origin);
		}
	}
}
