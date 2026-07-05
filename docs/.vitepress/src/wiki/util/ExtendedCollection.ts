
import { Collection, Comparator } from "@discordjs/collection";
import crypto from "crypto";
import hash from "object-hash";

import { BiFunction, Function } from "./FunctionTypes";
import Util from "./Util";

class ExtendedCollection<K, V> extends Collection<K, V> {
	public constructor();
	public constructor(iterable: Iterable<readonly [K, V]>);
	public constructor(entries?: readonly (readonly [K, V])[] | null);
	public constructor(data?: Iterable<readonly [K, V]> | (readonly [K, V])[] | null) {
		super(data);

		for (const key of this.keys()) this.#HASH_KEY_MAP.set(this.computeHash(key), key);
	}

	private static _defaultSort(firstValue, secondValue) {
		return Number(firstValue > secondValue) || Number(firstValue === secondValue) - 1;
	}

	#HASH_METHOD: HashName | undefined = "md5";
	#HASH_KEY_MAP: Map<string, K> = new Map();

	private computeHash(key: K): string {
		return hash(key as any, { algorithm: this.#HASH_METHOD as any });
	}

	private resolveKey(key: K, set?: boolean, del?: boolean): K {
		if (!this.#HASH_METHOD || super.has(key)) return key;

		const hash = this.computeHash(key);

		if (!this.#HASH_KEY_MAP.has(hash) && set) this.#HASH_KEY_MAP.set(hash, key);

		if (!del) {
			return this.#HASH_KEY_MAP.get(hash) as K;
		} else {
			const value = this.#HASH_KEY_MAP.get(hash);

			this.#HASH_KEY_MAP.delete(hash);

			return value as K;
		}
	}

	private recomputeHashes(): void {
		const values = new Set(this.keys());

		this.#HASH_KEY_MAP.clear();

		for (const value of values) this.#HASH_KEY_MAP.set(this.computeHash(value), value);
	}

	public setHashMethod(): void;
	public setHashMethod(method: HashName): void;
	public setHashMethod(method?: HashName): void {
		if (!method) return void (this.#HASH_METHOD = method);

		if (!crypto.getHashes().includes(method))
			throw new RangeError(`The hashing method: ${method} cannot be resolved! Use crypto.getHashes() to get a list of all valid hashing methods!`);

		this.#HASH_METHOD = method;
		this.recomputeHashes();
	}

	public override get(key: K): V | undefined {
		return super.get(this.resolveKey(key)) ?? undefined;
	}

	public override set(key: K, value: V): this {
		return super.set(this.resolveKey(key, true), value);
	}

	public override has(key: K): boolean {
		return super.has(this.resolveKey(key));
	}

	public override delete(key: K): boolean {
		return super.delete(this.resolveKey(key, false, true));
	}

	// Collection methods that need copying over (due to Collection using Map impl. via super#xxxx)
	public override ensure(key: K, defaultValueGenerator: BiFunction<K, this, V>): V {
		return super.ensure(this.resolveKey(key), defaultValueGenerator);
	}

	public override hasAll(...keys: K[]) {
		return keys.every(key => this.has(key));
	}

	public override hasAny(...keys: K[]): boolean {
		return keys.some(key => this.has(key));
	}

	// eslint-disable-next-line no-underscore-dangle
	public override sort(compareFunction: Comparator<K, V> = ExtendedCollection._defaultSort): this {
		const entries = [...this.entries()];

		entries.sort((a, b) => compareFunction(a[1], b[1], a[0], b[0]));

		this.clear();
		for (const [key, value] of entries)
			this.set(key, value);

		return this;
	}



	/**
	 * Removes the mapping for a key from this map if it is present (optional operation).
	 * 
	 * Returns the value to which this map previously associated the key, or `undefined` if the map
	 * contained no mapping for the key.
	 * 
	 * If you want to only obtain whether or not the key has existed prior to deletion, use 
	 * `ExtendedCollection#delete` instead.
	 * 
	 * @param key The key whose mapping is to be removed from the map
	 * @returns `true` if an element in the Map existed and has been removed, or `false` if the element does not exist.
	 */
	public remove(key: K): V | undefined {
		const value = this.get(key);
		this.delete(key);
		return value;
	}

	/**
	 * Returns the value to which the specified key is mapped, or `defaultValue` if this map
	 * contains no mapping for the key.
	 * 
	 * @param key The key whose associated value is to be returned.
	 * @param defaultValue The default mapping of the key.
	 * @returns The value to which the specified key is mapped, or `defaultValue` if this map contains no mapping for the key.
	 */
	public getOrDefault(key: K, defaultValue: V): V {
		return this.get(key) ?? defaultValue;
	}

	/**
	 * If the specified key is not already associated with a value (or is mapped to `undefined`),
	 * associates it with the given value and returns `undefined`, else returns the current value.
	 * 
	 * @param key The key with which the specified value is to be associated.
	 * @param value The value to be associated with the specified key, if not associated yet.
	 * @returns The previous value associated with the specified key, or `undefined` if there was no mapping for the key.
	 */
	public putIfAbsent(key: K, value: V): V | undefined {
		const previousValue = this.get(key);

		this.set(key, previousValue ?? value);

		return previousValue;
	}

	/**
	 * An alias for `ExtendedCollection.putIfAbsent`, if the specified key is not already 
	 * associated with a value (or is mapped to `undefined`), associates it with the given value and
	 * returns `undefined`, else returns the current value.
	 * 
	 * @param key The key with which the specified value is to be associated.
	 * @param value The value to be associated with the specified key, if not associated yet.
	 * @returns The previous value associated with the specified key, or `undefined` if there was no mapping for the key.
	 */
	public setIfAbsent(key: K, value: V): V | undefined {
		return this.putIfAbsent(key, value) ?? undefined;
	}

	/**
	 * If the specified key is not already associated with a value (or is mapped to `undefined`),
	 * attempts to compute its value using the given mapping function and enters it into this map
	 * unless `undefined`. Conversely, if the value for the specified key is present and non-undefined,
	 * attempts to compute a new mapping given the key and its current mapped value.
	 * 
	 * If the mapping function returns `undefined` and the specified key is not already associated with
	 * a value (or is mapped to `undefined`), no mapping is recorded. If the specified key is present
	 * and non-undefined, the mapping is removed. If the mapping function itself throws an (unchecked)
	 * error, the error is rethrown, and no mapping is recorded.
	 * 
	 * The combination method of both `ExtendedCollection#computeIfAbsent` and 
	 * `ExtendedCollection#computeIfPresent`.
	 * 
	 * @param key The key with which the specified value is to be associated.
	 * @param absentMapper The mapping function to compute a value. This function is only called when the specified key is not already associated with a value (or is mapped to `undefined`).
	 * @param presentRemapper The remapping function to compute a value. This function is only called if the value for the specified key is present and non-null.
	 */
	public compute<V2 extends V, V3 extends V>(key: K, absentMapper: Function<K, V3>, presentRemapper: BiFunction<K, V, V2>): V | undefined {
		return this.has(key)
			? this.computeIfPresent(key, presentRemapper)
			: this.computeIfAbsent(key, absentMapper);
	}

	/**
	 * If the specified key is not already associated with a value (or is mapped to `undefined`),
	 * attempts to compute its value using the given mapping function and enters it into this map
	 * unless `null` or `undefined`.
	 * 
	 * If the mapping function returns `null` or `undefined`, no mapping is recorded. If the 
	 * mapping function itself throws an (unchecked) error, the error is rethrown, and no mapping
	 * is recorded.
	 * 
	 * @param key The key with which the specified value is to be associated.
     * @param mappingFunction The mapping function to compute a value.
     * @return The current (existing or computed) value associated with the specified key, or `undefined` if the computed value is `undefined`.
	 */
	public computeIfAbsent<V2 extends V>(key: K, mappingFunction: Function<K, V2>): V | V2 {
		const value = this.get(key);

		if (value !== undefined) return value;

		const computed = mappingFunction(key);

		if (!Util.isNullOrUndefined(computed)) this.set(key, computed);

		return computed;
	}

	/**
     * If the value for the specified key is present and non-undefined, attempts to compute a new
	 * mapping given the key and its current mapped value.
     *
     * If the remapping function returns `null` or `undefined`, the mapping is removed.
     * If the remapping function itself throws an error, the error is rethrown, and the current
	 * mapping is left unchanged.
	 * 
     * @param key The key with which the specified value is to be associated.
     * @param remappingFunction The remapping function to compute a value.
     * @return The new value associated with the specified key, or `undefined` if none.
	 */
	public computeIfPresent<V2 extends V>(key: K, remappingFunction: BiFunction<K, V, V2>): V | undefined {
		const value = this.get(key);

		if (value === undefined)
			return value;

		const computed = remappingFunction(key, value);

		if (Util.isNullOrUndefined(computed))
			this.delete(key);
		else
			this.set(key, computed);

		return computed;
	}
}

export type HashName =
	| "sha1"
	| "md5"
	| "passthrough"

export default ExtendedCollection;