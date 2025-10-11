package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

/*
 * DEV NOTE: No concept of "Poise" exists within Seven Elements, therefore the implementation of
 * Shatter has been modified slightly.
 *
 * When the target takes **any** Geo DMG (considering ICD) or when they are hit with a "Heavy Weapon",
 * Shatter is triggered and the Frozen Aura is removed.
 */
public abstract sealed class AbstractShatterElementalReaction
	extends ElementalReaction
	permits GeoShatterElementalReaction, HeavyShatterElementalReaction
{
	AbstractShatterElementalReaction(Settings settings) {
		super(settings);
	}

	@Override
	public boolean trigger(LivingEntity entity, @Nullable LivingEntity origin) {
		if (!isTriggerable(entity)) return false;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final ElementalApplication auraElement = component.getElementalApplication(this.auraElement.getLeft());
		final ElementalApplication triggeringElement = component.getElementalApplication(this.triggeringElement.getLeft());

		final double reducedGauge = auraElement.reduceGauge(Double.MAX_VALUE);

		this.onTrigger(entity, auraElement, triggeringElement, reducedGauge, origin);

		return true;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		if (!(entity.getWorld() instanceof final ServerWorld world)) return;

		final float damage = ElementalReaction.getReactionDamage(entity, 3.0);
		final ElementalDamageSource source = new ElementalDamageSource(
			entity
				.getDamageSources()
				.create(SevenElementsDamageTypes.SHATTER, origin),
			ElementalApplications.gaugeUnits(entity, Element.PHYSICAL, 0.0, false),
			InternalCooldownContext.ofNone(entity)
		).shouldApplyDMGBonus(false).shouldInfuse(false);

		entity.damage(world, source, damage);
		entity.removeStatusEffect(SevenElementsStatusEffects.FROZEN);
	}
}
