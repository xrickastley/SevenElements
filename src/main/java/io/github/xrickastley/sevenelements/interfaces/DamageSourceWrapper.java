package io.github.xrickastley.sevenelements.interfaces;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.damagesource.DamageSource;

/**
 * An interface for classes that wrap around Minecraft's {@link DamageSource} class.
 */
public interface DamageSourceWrapper {
	/**
	 * Recursively gets the possible {@code DamageSource} instances of the provided
	 * {@code DamageSource} and return them as a {@code Stream}. <br> <br>
	 *
	 * The {@code Stream} will terminate when
	 * {@link DamageSourceWrapper#getOriginalSource DamageSourceWrapper#getOriginalSource} returns
	 * a value that is not an instance of {@code DamageSource}, normally {@code null}.
	 *
	 * @param source The {@code DamageSource} to recursively get all possible {@code DamageSource} instances of.
	 */
	public static Stream<DamageSource> getDamageSources(final DamageSource source) {
		return Stream.iterate(
			source,
			s -> s instanceof DamageSource,
			s -> s instanceof final DamageSourceWrapper wrapper
				? wrapper.getOriginalSource()
				: null
		);
	}

	/**
	 * Returns the {@code DamageSource} that was used to create this {@code DamageSourceWrapper},
	 * or {@code null} if a {@code DamageSource} wasn't used.
	 */
	public @Nullable DamageSource getOriginalSource();
}
