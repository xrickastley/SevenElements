package io.github.xrickastley.sevenelements.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.github.xrickastley.sevenelements.util.functions.TriConsumer;
import io.github.xrickastley.sevenelements.util.functions.TriFunction;
import io.github.xrickastley.sevenelements.util.functions.TriPredicate;

public final class Functions {
	public static <A, B, R> Function<A, R> compose(Function<A, B> fn1, Function<B, R> fn2) {
		return fn2.compose(fn1);
	}

	public static <A, B, C, R> Function<A, R> compose(Function<A, B> fn1, Function<B, C> fn2, Function<C, R> fn3) {
		return fn3.compose(fn2.compose(fn1));
	}

	public static <A, B, C, D, R> Function<A, R> compose(Function<A, B> fn1, Function<B, C> fn2, Function<C, D> fn3, Function<D, R> fn4) {
		return fn4.compose(fn3.compose(fn2.compose(fn1)));
	}

	public static <A, B> Predicate<A> composePredicate(Function<A, B> fn1, Predicate<B> fn2) {
		return a -> fn2.test(fn1.apply(a));
	}

	public static <A, B, C> Predicate<A> composePredicate(Function<A, B> fn1, Function<B, C> fn2, Predicate<C> fn3) {
		return a -> fn3.test(fn2.compose(fn1).apply(a));
	}

	public static <A, B, C, D> Predicate<A> composePredicate(Function<A, B> fn1, Function<B, C> fn2, Function<C, D> fn3, Predicate<D> fn4) {
		return a -> fn4.test(fn3.compose(fn2.compose(fn1)).apply(a));
	}

	public static <A, B> Consumer<A> composeConsumer(Function<A, B> fn1, Consumer<B> fn2) {
		return a -> fn2.accept(fn1.apply(a));
	}

	public static <A, B, C> Consumer<A> composeConsumer(Function<A, B> fn1, Function<B, C> fn2, Consumer<C> fn3) {
		return a -> fn3.accept(fn2.compose(fn1).apply(a));
	}

	public static <A, B, C, D> Consumer<A> composeConsumer(Function<A, B> fn1, Function<B, C> fn2, Function<C, D> fn3, Consumer<D> fn4) {
		return a -> fn4.accept(fn3.compose(fn2.compose(fn1)).apply(a));
	}

	public static <T, R> Supplier<R> map(Supplier<T> supplier, Function<T, R> mapper) {
		return () -> mapper.apply(supplier.get());
	}

	public static <T, R> Consumer<T> consumer(Function<T, R> fn) {
		return fn::apply;
	}

	public static <T> Predicate<T> predicate(Function<T, Boolean> fn) {
		return fn::apply;
	}

	public static <T, R> Supplier<R> supplier(Function<T, R> fn, T value) {
		return () -> fn.apply(value);
	}

	public static <T, U, R> Supplier<R> supplier(BiFunction<T, U, R> fn, T t, U u) {
		return () -> fn.apply(t, u);
	}

	public static <T, U, V, R> Supplier<R> supplier(TriFunction<T, U, V, R> fn, T t, U u, V v) {
		return () -> fn.apply(t, u, v);
	}

	public static <T, R> Runnable runnable(Function<T, R> fn, T value) {
		return () -> fn.apply(value);
	}

	public static <T, R> Runnable runnable(Consumer<T> fn, T value) {
		return () -> fn.accept(value);
	}

	public static <T, U, R> Function<T, R> withArgument(BiFunction<T, U, R> fn, U value) {
		return t -> fn.apply(t, value);
	}

	public static <T, U, V, R> Function<T, R> withArgument(TriFunction<T, U, V, R> fn, U u, V v) {
		return t -> fn.apply(t, u, v);
	}

	public static <T, U> Consumer<T> withArgument(BiConsumer<T, U> fn, U value) {
		return t -> fn.accept(t, value);
	}

	public static <T, U, V> Consumer<T> withArgument(TriConsumer<T, U, V> fn, U u, V v) {
		return t -> fn.accept(t, u, v);
	}

	public static <T, U> Predicate<T> withArgument(BiPredicate<T, U> fn, U value) {
		return t -> fn.test(t, value);
	}

	public static <T, U, V> Predicate<T> withArgument(TriPredicate<T, U, V> fn, U u, V v) {
		return t -> fn.test(t, u, v);
	}
}
