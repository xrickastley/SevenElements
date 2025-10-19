package io.github.xrickastley.sevenelements.interfaces;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.damage.DamageSource;

/**
 * An interface for classes that wrap around Minecraft's {@link DamageSource} class.
 */
public interface DamageSourceWrapper {
	/**
	 * Returns the {@code DamageSource} that was used to create this {@code DamageSourceWrapper},
	 * or {@code null} if a {@code DamageSource} wasn't used.
	 */
	public @Nullable DamageSource getOriginalSource();
}
