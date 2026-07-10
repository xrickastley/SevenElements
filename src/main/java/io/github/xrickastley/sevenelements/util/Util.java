package io.github.xrickastley.sevenelements.util;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

public final class Util {
	public static boolean isCalledBy(Class<?> clazz) {
		return Util.isCalledBy(clazz, null, 0);
	}

	public static boolean isCalledBy(Class<?> clazz, int offset) {
		return Util.isCalledBy(clazz, null, offset);
	}

	public static boolean isCalledBy(Class<?> clazz, @Nullable String method) {
		return Util.isCalledBy(clazz, method, 0);
	}

	public static boolean isCalledBy(Class<?> clazz, @Nullable String method, int offset) {
		return Util.isCalledBy(clazz.getName(), method, offset);
	}

	public static boolean isCalledBy(String className, @Nullable String method, int offset) {
		// offset accounting getStackTrace() + this method + calling method
		offset += 3;

		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		if (offset >= stackTrace.length) return false;

		final StackTraceElement caller = stackTrace[offset];

		return caller.getClassName().equals(className) && (method == null || caller.getMethodName().equals(method));
	}



	/**
	 * To normally be used with {@link Stream#filter()}.
	 *
	 * <p>Imitates {@link Stream#distinct()}, except with a mapping argument. The predicate
	 * returns {@code true} if the mapped element is distinct with the {@code Predicate} instance
	 * created by this method, {@code false} otherwise.
	 *
	 * @param <T> The type of the input.
	 * @param <K> The type of the result after mapping the input.
	 * @param keyMapper A function that transforms the input into an element to be used for
	 * distinct element comparison.
	 */
	public static <T, K> Predicate<T> distinctKeyed(Function<T, ? extends K> keyMapper) {
		final Set<K> seen = ConcurrentHashMap.newKeySet();

		return t -> seen.add(keyMapper.apply(t));
	}
}
