package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.annotation.mixin.At;
import io.github.xrickastley.sevenelements.annotation.mixin.Inject;
import io.github.xrickastley.sevenelements.annotation.mixin.Local;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.entity.LivingEntity;

public final class QuickenElementalReaction extends ElementalReaction {
	QuickenElementalReaction() {
		super(
			new Settings("Quicken", SevenElements.identifier("quicken"), TextHelper.reaction("reaction.seven-elements.quicken", "#01e858"))
				.setReactionCoefficient(1.0)
				.setAuraElement(Element.DENDRO, 2)
				.setTriggeringElement(Element.ELECTRO, 8)
				.reversable(true)
				.preventsPriorityUpgrade(true)
				.preventsReactionsAfter(SevenElements.identifier("spread"), SevenElements.identifier("aggravate"))
	);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final double quickenAuraGauge = Math.min(auraElement.getCurrentGauge() + reducedGauge, triggeringElement.getCurrentGauge() + reducedGauge);
		final double tickDuration = (quickenAuraGauge * 5 + 6) * 20;

		ElementComponent.KEY
			.get(entity)
			.getElementHolder(Element.QUICKEN)
			.setElementalApplication(
				ElementalApplications.duration(entity, Element.QUICKEN, quickenAuraGauge, tickDuration)
			);
	}

	// These "mixins" are injected pieces of code that allow Quicken to work properly, and allow code readers to easily see the way it was hardcoded.
	@Inject(
		method = "Lio/github/xrickastley/sevenelements/component/ElementComponentImpl;attemptReapply(Lio/github/xrickastley/sevenelements/element/ElementalApplication;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lio/github/xrickastley/sevenelements/component/ElementComponentImpl;getElementalApplication(Lio/github/xrickastley/sevenelements/element/Element;)Lio/github/xrickastley/sevenelements/element/ElementalApplication;",
			shift = At.Shift.AFTER
		)
	)
	public static boolean mixin$preventReapplication(@Local(argsOnly = true) ElementalApplication application, @Local(self = true) ElementComponent component) {
		return (application.getElement() == Element.ELECTRO || application.getElement() == Element.DENDRO)
			&& component.hasElementalApplication(Element.QUICKEN);
	}
}
