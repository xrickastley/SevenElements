package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.ElementalApplication;

import net.minecraft.entity.LivingEntity;

public abstract class AmplifyingElementalReaction extends ElementalReaction {
	final double amplifier;

	protected AmplifyingElementalReaction(Settings settings, double amplifier) {
		super(settings);

		this.amplifier = amplifier;
	}

	public float applyAmplifier(float damage) {
		return (float) applyAmplifier((double) damage);
	}

	public double applyAmplifier(double damage) {
		return damage * amplifier;
	}

	public double getAmplifier() {
		return this.amplifier;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {}
}
