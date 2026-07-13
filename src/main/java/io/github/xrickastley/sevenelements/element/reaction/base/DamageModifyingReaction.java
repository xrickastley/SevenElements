package io.github.xrickastley.sevenelements.element.reaction.base;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public sealed interface DamageModifyingReaction
	permits AdditiveElementalReaction, AmplifyingElementalReaction
{
	public float modifyDamage(@Nullable Entity origin, ServerLevel world, float damage);

	public Phase getPhase();

	public static enum Phase {
		BASE, TOTAL
	}
}
