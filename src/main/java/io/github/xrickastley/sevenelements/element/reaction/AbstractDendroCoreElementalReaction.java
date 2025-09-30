package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.events.ReactionTriggered;

import net.minecraft.entity.LivingEntity;

public abstract sealed class AbstractDendroCoreElementalReaction
	extends ElementalReaction
	permits HyperbloomElementalReaction, BurgeonElementalReaction
{
	AbstractDendroCoreElementalReaction(Settings settings) {
		super(settings);
	}

	@Override
	public boolean hasElement(Element element) {
		return false;
	}

	@Override
	public boolean isTriggerable(LivingEntity entity) {
		return entity instanceof DendroCoreEntity;
	}

	@Override
	public boolean trigger(LivingEntity entity, @Nullable LivingEntity origin) {
		if (!(entity instanceof final DendroCoreEntity dendroCore)) throw new ClassCastException("This reaction may only be triggered on a Dendro Core! Provided entity: " + entity);

		if (!dendroCore.isNormal()) return false;

		this.onReaction(dendroCore, origin);
		this.displayReaction(entity);

		ReactionTriggered.EVENT
			.invoker()
			.onReactionTriggered(this, 0, entity, origin);

		return true;
	}

	@Override
	protected void onReaction(LivingEntity entity, @Nullable ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {}

	protected abstract void onReaction(DendroCoreEntity dendroCore, @Nullable LivingEntity origin);
}
