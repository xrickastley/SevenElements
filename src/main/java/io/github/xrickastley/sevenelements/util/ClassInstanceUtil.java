package io.github.xrickastley.sevenelements.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

public final class ClassInstanceUtil {
	@SuppressWarnings("unchecked")
	public static <T> T cast(Object instance) {
		return (T) instance;
	}

	/**
	 * Casts the provided {@code instance} into an instance of {@code T} if it's an instance of
	 * {@code T}.
	 *
	 * @param <T> The type of the instance.
	 * @param instance The object to cast into an instance of {@code T}
	 * @param castClass The target class of the cast.
	 * @return {@code instance} as an instance of {@code T}, {@code null} otherwise.
	 */
	@SuppressWarnings("unchecked")
	public static <T> @Nullable T castOrNull(Object instance, Class<T> castClass) {
		return castClass.isInstance(instance)
			? (T) instance
			: null;
	}

	/**
	 * Maps the provided instance into {@code R} if it isn't {@code null}.
	 *
	 * @param <T> The type of the instance.
	 * @param <R> The type of the result.
	 * @param instance The instance of {@code T} to map if it isn't null.
	 * @param mapper The mapper to apply if {@code instance} isn't null
	 * @return {@code R} if {@code instance} isn't {@code null}, {@code null} otherwise.
	 */
	public static <T, R> @Nullable R mapOrNull(@Nullable T instance, Function<T, R> mapper) {
		return instance == null
			? null
			: mapper.apply(instance);
	}

	/**
	 * If {@code instance} isn't null and {@code mapper.apply(instance)} isn't {@code null}, calls the
	 * {@code ifNonNull} consumer with the mapped value obtained from {@code mapper.apply(instance)}.
	 *
	 * @param <T> The type of the instance.
	 * @param <R> The type of the result.
	 * @param instance The instance of {@code T} to map if it isn't null.
	 * @param mapper The mapper to apply if {@code instance} isn't null
	 * @param ifNonNull The consumer to call if the result of mapping the instance with {@code mapper} isn't {@code null}.
	 */
	public static <T, R> void ifPresentMapped(@Nullable T instance, Function<T, R> mapper, Consumer<R> ifNonNull) {
		if (instance == null) return;

		final R value = mapper.apply(instance);

		if (value == null) return;

		ifNonNull.accept(value);
	}

	/**
	 * If {@code obj} isn't null and {@code obj} is an instance of {@code clazz}, calls the
	 * {@code ifClassInstance} consumer with {@code obj} as an instance of {@code clazz}.
	 *
	 * @param <T> The type of the expected class.
	 * @param obj The object to check as an instance of {@code clazz} if it isn't null.
	 * @param clazz The expected class {@code obj} is an instance of.
	 * @param ifClassInstance The consumer to call if {@code obj} is an instance of {@code clazz}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> void ifInstanceOf(@Nullable Object obj, Class<T> clazz, Consumer<T> ifClassInstance) {
		if (obj == null || !clazz.isInstance(obj)) return;

		ifClassInstance.accept((T) obj);
	}

	/**
	 * If {@code obj} isn't null, is an instance of {@code clazz} and {@code predicate.test(obj)}
	 * is {@code true}, calls the {@code ifInstanceAnd} consumer with {@code obj} as an instance of
	 * {@code clazz}.
	 *
	 * @param <T> The type of the expected class.
	 * @param obj The object to check as an instance of {@code clazz} if it isn't null.
	 * @param clazz The expected class {@code obj} is an instance of.
	 * @param predicate The test to perform on {@code obj} if it is an instance of {@code T}.
	 * @param ifInstanceAnd The consumer to call if {@code obj} is an instance of {@code clazz} and {@code predicate.test(obj)} is {@code true}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> void ifInstanceOfAnd(@Nullable Object obj, Class<T> clazz, Predicate<T> predicate, Consumer<T> ifInstanceAnd) {
		if (obj == null || !clazz.isInstance(obj) || !predicate.test((T) obj)) return;

		ifInstanceAnd.accept((T) obj);
	}

	/**
	 * Returns {@code true} if the arguments are equal to each other
	 * and {@code false} otherwise.
	 * If both arguments are {@code null}, {@code false} is returned.
	 * Otherwise, equality is determined by calling the
	 * {@link Objects#equals(Object, Object) Objects#equals} method with
	 * the arguments of this method.
	 *
	 * @param a an object
	 * @param b an object to be compared with {@code a} for equality
	 * @return {@code true} if the arguments are equal to each other
	 * and are both not null, {@code false} otherwise
	 *
	 * @see Object#equals(Object, Object)
	 */
	public static boolean nonNullEquals(@Nullable Object a, @Nullable Object b) {
		return (a != null && b != null)
			&& Objects.equals(a, b);
	}
}
