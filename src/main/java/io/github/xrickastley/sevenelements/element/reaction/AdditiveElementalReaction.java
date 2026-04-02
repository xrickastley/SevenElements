package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.ElementalApplication;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public abstract class AdditiveElementalReaction extends ElementalReaction {
	final double amplifier;

	protected AdditiveElementalReaction(Settings settings, double amplifier) {
		super(settings);

		this.amplifier = amplifier;
	}

	public float applyAmplifier(LivingEntity entity, float damage) {
		return entity.level() instanceof final ServerLevel world
			? (float) applyAmplifier(world, (double) damage)
			: damage;
	}

	public float applyAmplifier(ServerLevel world, float damage) {
		return (float) applyAmplifier(world, (double) damage);
	}

	public double applyAmplifier(LivingEntity entity, double damage) {
		return entity.level() instanceof final ServerLevel world
			? applyAmplifier(world, (double) damage)
			: damage;
	}

	public double applyAmplifier(ServerLevel world, double damage) {
		return getDamageBonus(world) + damage;
	}

	public double getAmplifier() {
		return this.amplifier;
	}

	public double getDamageBonus(ServerLevel world) {
		return ElementalReaction.getReactionDamage(world, amplifier);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {}
}
