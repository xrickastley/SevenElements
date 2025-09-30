package io.github.xrickastley.sevenelements.interfaces;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.LivingEntity;

public interface IBossBar {
	default void sevenelements$setEntity(@Nullable LivingEntity entity) {}

	default @Nullable LivingEntity sevenelements$getEntity() {
		return null;
	}
}
