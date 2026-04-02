package io.github.xrickastley.sevenelements.interfaces;

import net.minecraft.world.entity.LivingEntity;

public interface EntityAwareEffect {
	default void onRemoved(LivingEntity entity, int amplifier) {}
}
