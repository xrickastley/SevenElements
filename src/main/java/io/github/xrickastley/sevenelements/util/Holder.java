package io.github.xrickastley.sevenelements.util;

import java.util.function.Function;

public final class Holder<T> {
	private final T value;

	private Holder(T value) {
		this.value = value;
	}

	public static <T> Holder<T> of(T value) {
		return new Holder<>(value);
	}

	public T get() {
		return this.value;
	}

	public <R> R map(Function<T, R> mapper) {
		return mapper.apply(value);
	}
}
