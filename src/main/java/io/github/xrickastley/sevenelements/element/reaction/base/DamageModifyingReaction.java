package io.github.xrickastley.sevenelements.element.reaction.base;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

public sealed interface DamageModifyingReaction
	permits AdditiveElementalReaction, AmplifyingElementalReaction
{
	public float modifyDamage(@Nullable Entity origin, ServerWorld world, float damage);

	public Phase getPhase();

	public static enum Phase {
		BASE, TOTAL
	}
}
