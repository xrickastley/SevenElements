package io.github.xrickastley.sevenelements.util;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

/**
 * Utility class for JavaScript's "quirks".
 */
public final class JavaScriptUtil {
	/**
	 * Mimics JavaScript's "logical OR" (||) operator, returning the first <i>truthy</i> value as
	 * determined by {@link JavaScriptUtil#isTruthy(Object) JavaScriptUtil#isTruthy} or the last
	 * <i>falsy</i> value as determined by {@link JavaScriptUtil#isFalsy(Object) JavaScriptUtil#isFalsy}
	 * in the provided array.
	 *
	 * @param <T> The type of the array.
	 * @param values The values to perform the logical OR operation on.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T logicalOR(@Nullable T... values) {
		for (final T value : values) if (isTruthy(value)) return value;

		return values[values.length - 1];
	}

	/**
	 * Mimics JavaScript's "nullish coalesing" (??) operator, returning the first <b>non-null</b>
	 * value or {@code null} if all provided values are {@code null}.
	 *
	 * @param <T> The type of the array.
	 * @param values The values to perform the nullish coalesing operation on.
	 */
	@SuppressWarnings("unchecked")
	public static <T> @Nullable T nullishCoalesing(@Nullable T... values) {
		for (final T value : values) if (value != null) return value;

		return null;
	}

	/**
	 * Mimics JavaScript's "nullish coalesing" (??) operator, returning the first {@code Supplier}
	 * to produce a <b>non-null</b> value or {@code null} if all provided values are {@code null}. <br> <br>
	 *
	 * Provided {@code Supplier} instances are evaluated in insertion order. If a {@code Supplier}
	 * is encountered, such that {@link Supplier#get() Supplier#get} returns a <b>non-null</b> value,
	 * the value is immediately returned and evaluation is ended.
	 *
	 * @param <T> The type of the array.
	 * @param suppliers The {@code Suppliers} to perform the nullish coalesing operation on.
	 */
	@SuppressWarnings("unchecked")
	public static <T> @Nullable T nullishCoalesingFn(Supplier<? extends T>... suppliers) {
		for (final Supplier<? extends T> supplier : suppliers) {
			final T value = supplier.get();

			if (value != null) return value;
		}

		return null;
	}

	/**
	 * Mimics JavaScript's falsy coercion, returning {@code true} if the value is considered
	 * <i>falsy</i>. <br> <br>
	 *
	 * A value is considered <i>falsy</i> if:
	 * <ul>
	 * 	<li>it is {@code null},</li>
	 * 	<li>it is {@code false},</li>
	 * 	<li>it is an empty string ({@code ""}), or</li>
	 * 	<li>it is {@code 0}, {@code -0} or fulfills {@link Double#isNaN() Double#isNaN}.</li>
	 * </ul>
	 *
	 * If any of the provided conditions are fulfilled, the value is considered <i>falsy</i>.
	 *
	 * @param any The value to test.
	 */
	public static boolean isFalsy(@Nullable Object any) {
		return any == null
			|| (any instanceof final Boolean bool && !bool)
			|| (any instanceof final String string && string.isEmpty())
			|| (any instanceof final Number number && (Double.isNaN(number.doubleValue()) || number.doubleValue() == 0.0));
	}

	/**
	 * Mimics JavaScript's truthy coercion, returning {@code true} if the value is considered
	 * <i>truthy</i>. <br> <br>
	 *
	 * A value is considered <i>truthy</i> if {@code isFalsy(value)} is {@code false}, as <i>truthy</i>
	 * is a direct negation of <i>falsy</i>.
	 *
	 * @param any The value to test.
	 */
	public static boolean isTruthy(@Nullable Object any) {
		return !JavaScriptUtil.isFalsy(any);
	}
}
