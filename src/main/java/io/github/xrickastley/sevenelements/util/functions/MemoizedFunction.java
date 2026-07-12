package io.github.xrickastley.sevenelements.util.functions;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Represents a function that accepts one argument and produces a result.
 * Unlike {@link Function Function}, {@code MemoizedFunction} will cache the
 * results and reuse it when the function is called with the input
 * corresponding to the cached result.
 *
 * <p>You may access the inputs stored in the cache as well as their
 * corresponding results via {@link #getInputs()} and {@link #getResults()},
 * respectively.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 *
 * @see java.util.function.Consumer
 */
public class MemoizedFunction<T, R> implements Function<T, R> {
	private final Function<T, R> function;
	private final Map<T, R> cache = new ConcurrentHashMap<>();

	public MemoizedFunction(final Function<T, R> function) {
		this.function = function;
	}

	public R apply(T object) {
		return this.cache.computeIfAbsent(object, function);
	}

	public Set<T> getInputs() {
		return Collections.unmodifiableSet(this.cache.keySet());
	}

	public Collection<R> getResults() {
		return Collections.unmodifiableCollection(this.cache.values());
	}
}
