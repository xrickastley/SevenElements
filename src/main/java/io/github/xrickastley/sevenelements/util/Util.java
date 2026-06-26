package io.github.xrickastley.sevenelements.util;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.util.functions.MemoizedFunction;

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

	public static <T, R> MemoizedFunction<T, R> memoize(final Function<T, R> function) {
		return new MemoizedFunction<>(function);
	}
}
