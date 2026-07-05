
export type Runnable = () => void;

export type PromisifiedRunnable = () => Promise<void>;

/**
 * Represents a function that accepts one argument and produces a result.
 * 
 * @param T the type of the input to the function
 * @param R the type of the result of the function
 */
export type Function<T, R> = (t: T) => R;

/**
 * An alias for `Function` in case the base type `Function` needs to be used. Represents a function
 * that accepts one argument and produces a result.
 * 
 * @param T the type of the input to the function
 * @param R the type of the result of the function
 */
export type UniFunction<T, R> = Function<T, R>;

/**
 * Represents a function that accepts two arguments and produces a result.
 * This is the two-arity specialization of `Function`.
 * 
 * @param T the type of the first argument to the function
 * @param U the type of the second argument to the function
 * @param R the type of the result of the function
 */
export type BiFunction<T, U, R> = (t: T, u: U) => R;

/**
 * Represents a function that accepts three arguments and produces a result.
 * This is the three-arity specialization of `Function`.
 * 
 * @param T the type of the first argument to the function
 * @param U the type of the second argument to the function
 * @param V the type of the third argument to the function
 * @param R the type of the result of the function
 */
export type TriFunction<T, U, V, R> = (t: T, u: U, v: V) => R;

/**
 * Represents a function that accepts a single input argument and returns no result.
 * 
 * @param T the type of the input to the operation
 */
export type Consumer<T> = (t: T) => void | any;

/**
 * Represents a function that accepts a single input argument and returns no result.
 * This is the `Promise` specialization of `Consumer`.
 * 
 * @param T the type of the input to the operation
 */
export type PromisifiedConsumer<T> = (t: T) => Promise<void | any>;

/**
 * Represents a function that accepts two arguments and returns no *significant* result.
 * This is the two-arity specialization of `Consumer`.
 * 
 * @param T the type of the first argument to the operation
 * @param U the type of the second argument to the operation
 */
export type BiConsumer<T, U> = (t: T, u: U) => void | any;

/**
 * Represents a function that accepts two arguments and returns no *significant* result.
 * This is the `Promise` specialization of `BiConsumer`.
 * 
 * @param T the type of the first argument to the operation
 * @param U the type of the second argument to the operation
 */
export type PromisifiedBiConsumer<T, U> = (t: T, u: U) => Promise<void | any>;

/**
 * Represents a function that accepts three arguments and returns no *significant* result.
 * This is the three-arity specialization of `Consumer`.
 * 
 * @param T the type of the first argument to the operation
 * @param U the type of the second argument to the operation
 * @param V the type of the third argument to the operation
 */
export type TriConsumer<T, U, V> = (t: T, u: U, v: V) => void | any;

/**
 * Represents a function that accepts three arguments and returns no *significant* result.
 * This is the `Promise` specialization of `TriConsumer`.
 * 
 * @param T the type of the first argument to the operation
 * @param U the type of the second argument to the operation
 * @param V the type of the third argument to the operation
 */
export type PromisifiedTriConsumer<T, U, V> = (t: T, u: U, v: V) => Promise<void | any>;

/**
 * Represents a function that accepts a single input argument and returns a boolean result.
 * 
 * @param T the type of the input to the operation
 */
export type Predicate<T> = (t: T) => boolean;

/**
 * Represents a function that accepts a two arguments and returns a boolean result.
 * This is the two-arity specialization of `Predicate`.
 * 
 * @param T the type of the first argument to the operation
 * @param T the type of the second argument to the operation
 */
export type BiPredicate<T, U> = (t: T, u: U) => boolean;

/**
 * Represents a function that accepts a three arguments and returns a boolean result.
 * This is the three-arity specialization of `Predicate`.
 * 
 * @param T the type of the first argument to the operation
 * @param T the type of the second argument to the operation
 * @param T the type of the third argument to the operation
 */
export type TriPredicate<T, U, V> = (t: T, u: U, v: V) => boolean;

/**
 * This indicates that a value is "voidable", meaning it is not needed.
 * 
 * For functions, it indicates that you can return any value or no value, as the value it returns
 * should be discarded anyways.
 */
export type Voidable = void | any;

/**
 * This indicates that a value is "awaitable", meaning it can possibly be a `Promise`.
 * 
 * For functions, it indicates that it *can* return a `Promise` that resolves into the value `T`.
 */
export type Awaitable<T> = Promise<T> | PromiseLike<T> | T;

/**
 * Represents a class declaration `T`.
 */
export type Class<T> = abstract new(...any: any[]) => T;

/**
 * Represents a non-abstract class declaration `T`.
 */
export type NonAbstractClass<T> = new(...any: any[]) => T;

/**
 * Represents a function that returns a result.
 * 
 * @param R the result of the function
 */
export type Supplier<R> = () => R;