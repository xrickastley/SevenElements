package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.annotation.mixin.At;
import io.github.xrickastley.sevenelements.annotation.mixin.Local;
import io.github.xrickastley.sevenelements.annotation.mixin.ModifyExpressionValue;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.effect.ElementalStatusEffect;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementHolder;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.entity.LivingEntity;

public final class FrozenElementalReaction extends ElementalReaction {
	FrozenElementalReaction() {
		super(
			new Settings("Frozen", SevenElements.identifier("frozen"), TextHelper.reaction("reaction.seven-elements.frozen", "#b4ffff"))
				.setReactionCoefficient(0)
				.setAuraElement(Element.CRYO, 4)
				.setTriggeringElement(Element.HYDRO, 3)
				.reversable(true)
				.preventsReactionsAfter(SevenElements.identifier("shatter_geo"), SevenElements.identifier("shatter_heavy"))
		);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double _reducedGauge, @Nullable LivingEntity origin) {
		double reducedGauge;

		if (auraElement.getElement() == Element.HYDRO) {
			reducedGauge = auraElement.reduceGauge(1 * triggeringElement.getCurrentGauge());
		} else {
			reducedGauge = auraElement.reduceGauge(Double.MAX_VALUE * triggeringElement.getCurrentGauge());
		}

		// Gauge_FreezeAura = 2 * min(Gauge_OriginAura, Gauge_TriggerElement)
		// Always the min of both.
		final double freezeAuraGauge = 2 * triggeringElement.reduceGauge(reducedGauge);
		// Freeze Duration (Seconds)
		final double freezeTickDuration = this.getFreezeDuration(freezeAuraGauge, entity) * 20;

		final ElementalApplication application = ElementalApplications.duration(entity, Element.FREEZE, freezeAuraGauge, freezeTickDuration);
		final ElementHolder holder = ElementComponent.KEY
			.get(entity)
			.getElementHolder(Element.FREEZE);


		if (holder.hasElementalApplication()) {
			holder
				.getElementalApplication()
				.reapply(application);
		} else {
			holder.setElementalApplication(application);
		}

		ElementalStatusEffect.applyPossibleStatusEffect(application);
	}

	/**
	 * Returns the freeze duration in seconds.
	 */
	private double getFreezeDuration(double freezeAuraGauge, LivingEntity target) {
		final ElementComponent component = ElementComponent.KEY.get(target);
		final boolean reapplied = component.hasElementalApplication(Element.FREEZE);

		// = 2√(5 * freezeAuraGauge) + 4) - 4
		if (!reapplied) return 2.0 * Math.sqrt((5 * freezeAuraGauge) + 4) - 4;

		final double decayTimeModifier = component.getFreezeDecayTimeModifier();

		return Math.sqrt(20 * freezeAuraGauge + Math.pow(decayTimeModifier + 4, 2)) - decayTimeModifier - 4;
	}

	// These "mixins" are injected pieces of code that allow Frozen to work properly, and allow code readers to easily see the way it was hardcoded.
	@ModifyExpressionValue(
		method = "Lio/github/xrickastley/sevenelements/component/ElementComponentImpl;triggerReactions(Lio/github/xrickastley/sevenelements/element/ElementalApplication;Lnet/minecraft/entity/LivingEntity;)Ljava/util/Set;",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/stream/Stream;noneMatch(Ljava/util/function/Predicate;)Z"
		)
	)
	public static boolean mixin$modifySwirlReactions(final boolean original, final @Local(self = true) ElementComponent component, final @Local ElementalReaction reaction) {
		if (reaction != ElementalReactions.FROZEN_SWIRL
			|| !component.hasElementalApplication(Element.FREEZE)
			|| !component.hasElementalApplication(Element.HYDRO)
		) return original;

		final ElementalApplication hydroApp = component.getElementalApplication(Element.HYDRO);
		final ElementalApplication freezeApp = component.getElementalApplication(Element.FREEZE);

		// Allow Frozen Swirl (Double Swirl with both reactions) if this is a Hydro aura applied BEFORE Frozen.
		return hydroApp.getAppliedAt() < freezeApp.getAppliedAt();
	}
}
