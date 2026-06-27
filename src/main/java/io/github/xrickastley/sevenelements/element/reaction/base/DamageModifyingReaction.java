package io.github.xrickastley.sevenelements.element.reaction.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public sealed interface DamageModifyingReaction 
	permits AdditiveElementalReaction, AmplifyingElementalReaction
{
	default float modifyDamage(@NotNull Entity origin, float damage) {
		return this.modifyDamage(origin, origin.getWorld(), damage);
	}

	default float modifyDamage(World world, float damage) {
		return this.modifyDamage(null, world, damage);
	}

	public float modifyDamage(@Nullable Entity origin, World world, float damage);

	public Phase getPhase();

	public static enum Phase {
		BASE, TOTAL
	}
}
