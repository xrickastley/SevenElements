package io.github.xrickastley.sevenelements.util.functions;

/**
 * Represents an operation that accepts three input arguments and returns no result.
 * This is the three-arity specialization of {@link java.util.function.Consumer}. <br> <br>
 * 
 * Unlike most other functional interfaces, {@code BiConsumer} is expected
 * to operate via side effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object, Object)}.
 *
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 *
 * @see java.util.function.Consumer
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     */
    void accept(T t, U u, V v);
}