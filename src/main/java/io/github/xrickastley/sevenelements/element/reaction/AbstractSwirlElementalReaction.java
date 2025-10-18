package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

public abstract sealed class AbstractSwirlElementalReaction
	extends ElementalReaction
	permits PyroSwirlElementalReaction, HydroSwirlElementalReaction, ElectroSwirlElementalReaction, CryoSwirlElementalReaction, FrozenSwirlElementalReaction
{
	private final Element swirlElement;
	private final boolean elementalAbsorptionOnly;

	/**
	 * Creates a Swirl reaction with the specified settings. <br> <br>
	 *
	 * The specified <b>aura element</b> will serve as the "swirlable" element. <br> <br>
	 *
	 * For example, if the Aura Element is {@link Element#PYRO}, then the Pyro element is swirled
	 * and spread to nearby targets (r=3m). <br> <br>
	 *
	 * For the Gauge Units applied by the Swirl reaction, as well as its duration, you may refer
	 * here: <a href=https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory/Advanced_Mechanics#Swirl_Elemental_Application">
	 * Swirl Elemental Application</a>
	 *
	 * @param settings The {@code Settings} for this {@code ElementalReaction}.
	 */
	AbstractSwirlElementalReaction(Settings settings) {
		this(settings, settings.getAuraElement());
	}

	/**
	 * Creates a Swirl reaction with the specified settings. <br> <br>
	 *
	 * The "swirlable" element is the spread element upon triggering the swirl reaction. <br> <br>
	 *
	 * For example, if the swirlable Element is {@link Element#PYRO}, then the Pyro element is
	 * swirled and spread to nearby targets (r=3m). <br> <br>
	 *
	 * For the Gauge Units applied by the Swirl reaction, as well as its duration, you may refer
	 * here: <a href=https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory/Advanced_Mechanics#Swirl_Elemental_Application">
	 * Swirl Elemental Application</a>
	 *
	 * @param settings The {@code Settings} for this {@code ElementalReaction}.
	 * @param elementalAbsorptionOnly Whether Swirl will <i>only</i> deal its Elemental Absorption
	 * damage to the Swirl target instead, i.e. the entity the Swirl reaction was triggered on.
	 */
	AbstractSwirlElementalReaction(Settings settings, boolean elementalAbsorptionOnly) {
		super(settings);

		this.swirlElement = settings.getAuraElement();
		this.elementalAbsorptionOnly = elementalAbsorptionOnly;
	}

	/**
	 * Creates a Swirl reaction with the specified settings. <br> <br>
	 *
	 * The "swirlable" element is the spread element upon triggering the swirl reaction. <br> <br>
	 *
	 * For example, if the swirlable Element is {@link Element#PYRO}, then the Pyro element is
	 * swirled and spread to nearby targets (r=3m). <br> <br>
	 *
	 * For the Gauge Units applied by the Swirl reaction, as well as its duration, you may refer
	 * here: <a href=https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory/Advanced_Mechanics#Swirl_Elemental_Application">
	 * Swirl Elemental Application</a>
	 *
	 * @param settings The {@code Settings} for this {@code ElementalReaction}.
	 * @param swirlElement The element to Swirl.
	 */
	AbstractSwirlElementalReaction(Settings settings, Element swirlElement) {
		super(settings);

		this.swirlElement = swirlElement;
		this.elementalAbsorptionOnly = false;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		if (!(entity.getEntityWorld() instanceof final ServerWorld world)) return;

		final double gaugeOriginAura = auraElement.getCurrentGauge() + reducedGauge;
		final double gaugeAnemo = triggeringElement.getCurrentGauge() + reducedGauge;

		final double gaugeReaction = gaugeOriginAura >= (0.5 * gaugeAnemo)
			? gaugeAnemo
			: gaugeOriginAura;

		final double gaugeSwirlAttack = ((gaugeReaction - 0.04) * 1.25) + 1;

		for (final LivingEntity target : ElementalReaction.getEntitiesInAoE(entity, 6, t -> t != origin)) {
			final float damage = !elementalAbsorptionOnly || target == entity
				? ElementalReaction.getReactionDamage(entity, 0.6)
				: 0f;

			/*
			 * There isn't much documentation on the DMG of Elemental Absorption, but from tests,
			 * there *are* instances in which Swirl DMG = Elemental Absorption DMG. As such, it is
			 * used instead.
			 */

			final ElementalDamageSource source = new ElementalDamageSource(
				entity
					.getDamageSources()
					.create(SevenElementsDamageTypes.SWIRL, origin),
				ElementalApplications.gaugeUnits(target, swirlElement, target == entity ? 0f : gaugeSwirlAttack, true),
				InternalCooldownContext.ofNone(origin)
			).shouldApplyDMGBonus(false);

			target.damage(world, source, damage);
		}
	}
}
