package io.github.xrickastley.sevenelements.util.functions;

/**
 * Represents a predicate (boolean-valued function) of three arguments.
 * This is the three-arity specialization of {@link java.util.function.Predicate}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #test(Object, Object, Object)}.
 *
 * @param <T> the type of the first argument to the predicate
 * @param <U> the type of the second argument the predicate
 *
 * @see java.util.function.Predicate
 */
@FunctionalInterface
public interface TriPredicate<T, U, V> {
    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @return {@code true} if the input arguments match the predicate,
     * otherwise {@code false}
     */
    boolean test(T t, U u, V v);
}
