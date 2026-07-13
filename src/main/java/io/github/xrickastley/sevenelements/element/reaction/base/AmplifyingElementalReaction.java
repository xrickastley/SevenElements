package io.github.xrickastley.sevenelements.element.reaction.base;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction.Settings;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public non-sealed abstract class AmplifyingElementalReaction
	extends ElementalReaction
	implements DamageModifyingReaction
{
	protected AmplifyingElementalReaction(Settings settings) {
		super(
			settings
				.setType(Type.AMPLIFYING)
		);
	}

	/**
	 * {@deprecated Use {@link Settings#setReactionMultiplier()} instead.}
	 */
	@Deprecated
	protected AmplifyingElementalReaction(Settings settings, double amplifier) {
		this(settings.setReactionMultiplier(amplifier));
	}

	/**
	 * {@deprecated Use {@link ElementalReaction#getReactionMultiplier()} instead.}
	 */
	@Deprecated
	public final double getAmplifier() {
		return this.getReactionMultiplier();
	}

	@Override
	public final float getReactionStrength(@Nullable Entity origin, ServerLevel world) {
		return (float) (this.getReactionMultiplier() * this.getBonusReactionMultiplier(origin));
	}

	@Override
	public final float modifyDamage(@Nullable Entity origin, ServerLevel world, float damage) {
		return damage * this.getReactionStrength(origin, world);
	}

	@Override
	public final Phase getPhase() {
		return Phase.TOTAL;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {}
}
