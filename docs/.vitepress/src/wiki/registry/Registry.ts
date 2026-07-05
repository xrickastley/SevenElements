
import { BaseValidator, Result, s } from "@sapphire/shapeshift";

import RegistryEntry from "./RegistryEntry";
import RegistryKey from "./RegistryKey";
import RegistryKeys from "./RegistryKeys";
import TagKey from "./TagKey";

import { Consumer, Supplier } from "../util/FunctionTypes";
import ExtendedCollection from "../util/ExtendedCollection";
import Identifier from "../util/Identifier";

class Registry<T> {
	public constructor(key: RegistryKey<Registry<T>>)
	public constructor(key: RegistryKey<Registry<T>>, entryCodec: Supplier<BaseValidator<T>>)
	public constructor(key: RegistryKey<Registry<T>>, entryCodec?: Supplier<BaseValidator<T>>) {
		this.key = key;
		this.entryCodec = entryCodec;
		this.entries = new ExtendedCollection();
		this.registryEntries = new ExtendedCollection();
		this.tags = new ExtendedCollection();
		this.frozen = false;

		if (key.getValue() === RegistryKeys.ROOT)
			Registry.#ROOT = this as Registry<Registry<unknown>>;
		else
			Registry.register(Registry.#ROOT, key, this);
	}

	static #ROOT: Registry<Registry<unknown>>;

	private readonly key: RegistryKey<Registry<T>>;
	private readonly entryCodec?: Supplier<BaseValidator<T>>;
	protected readonly entries: ExtendedCollection<RegistryKey<T>, T>;
	protected readonly registryEntries: ExtendedCollection<RegistryKey<T>, RegistryEntry<T>>;
	protected readonly tags: ExtendedCollection<TagKey<T>, (TagKey<T> | T)[]>;
	protected frozen: boolean;

	private static resolveAsKey<T>(registry: Registry<T>, idOrKey: Identifier | RegistryKey<T>): RegistryKey<T> {
		return idOrKey instanceof Identifier
			? RegistryKey.of(registry.getRegistryKey(), idOrKey)
			: idOrKey;
	}

	public static register<T>(registry: Registry<T>, id: Identifier, entry: T): T;
	public static register<T>(registry: Registry<T>, key: RegistryKey<T>, entry: T): T;
	public static register<T>(registry: Registry<T>, idOrKey: Identifier | RegistryKey<T>, entry: T): T {
		const key = Registry.resolveAsKey(registry, idOrKey);

		if (registry.contains(key))
			throw new Error(`Tried to register already existing key: ${key}!`);

		if (!key.getRegistry().equals(registry.key.getValue()))
			throw new Error(`Tried to register invalid key: ${key} to ${registry}!`);

		if (registry.frozen)
			throw new Error(`Cannot register entries to a frozen registry!`);

		if (registry.entryCodec) {
			const result = registry.entryCodec().run(entry);

			if (result.error)
				throw new Error(`Error while registering registry entry ${idOrKey}!`, { cause: result.error });
		}

		registry.entries.set(key, entry);

		return entry;
	}

	public static registerTag<T>(registry: Registry<T>, key: TagKey<T>, entries: (TagKey<T> | T)[]): void {
		if (registry.tags.has(key))
			throw new Error(`Tried to register already existing tag key: ${key}!`);

		if (!key.isOf(registry.key))
			throw new Error(`Tried to register invalid tag key: ${key} to ${registry}!`);

		if (registry.frozen)
			throw new Error(`Cannot register tags to a frozen registry!`);

		registry.tags.set(key, [...entries]);
	}

	private validateTags() {
		const loadedTags: Set<TagKey<T>> = new Set(this.tags.keys());
		const state: ExtendedCollection<TagKey<T>, State> = new ExtendedCollection();

		for (const [tag] of this.tags)
			if (!this.validateTag(tag, state))
				this.tags.remove(tag);

		const invalidTags = state
			.filter((state, key) => state !== State.VALID && loadedTags.has(key))
			.map((_state, key) => `#${key.getId()}`);

		if (invalidTags.length > 0)
			console.warn(`Removed ${invalidTags.length} invalid tags: [${invalidTags.join(", ")}]`);
	}

	private validateTag(tag: TagKey<T>, state: ExtendedCollection<TagKey<T>, State>): boolean {
		switch (state.get(tag)) {
			case State.VALID:
				return true;
			case State.INVALID:
			case State.VISITING:
				return false;
			default:
				break;
		}

		state.set(tag, State.VISITING);

		const entries = this.tags.get(tag);

		if (entries === undefined) {
			state.set(tag, State.INVALID);
			return false;
		}

		for (const entry of entries) {
			if (entry instanceof TagKey && !this.validateTag(entry, state)) {
				state.set(entry, State.INVALID);
				return false;
			}
		}

		state.set(tag, State.VALID);
		return true;
	}

	private validateEntries() {
		for (const entry of this.registryEntries.values())
			entry.validate();
	}



	public contains(key: RegistryKey<T>): boolean {
		return this.entries.has(key);
	}

	public containsId(id: Identifier): boolean {
		return this.contains(RegistryKey.of(this.key, id));
	}

	public forEach(action: (entry: T) => void): void {
		this.entries.forEach(action);
	}

	public freeze(): this {
		this.validateTags();
		this.validateEntries();

		this.frozen = true;

		return this;
	}

	public get(id: Identifier): T | null;
	public get(key: RegistryKey<T>): T | null;
	public get(idOrKey: Identifier | RegistryKey<T>): T | null {
		return this.entries.get(Registry.resolveAsKey(this, idOrKey)) ?? null;
	}

	public getOrThrow(id: Identifier): T;
	public getOrThrow(key: RegistryKey<T>): T;
	public getOrThrow(idOrKey: Identifier | RegistryKey<T>): T {
		const key: RegistryKey<T> = Registry.resolveAsKey(this, idOrKey);
		const entry: T | null = this.get(key);

		if (!entry)
			throw new Error(`No registry entry with registry key: ${key} exists in this ${this}`);

		return entry;
	}

	public getId(entry: T): Identifier | null {
		return this.getKey(entry)?.getValue() ?? null;
	}

	public getKey(entry: T): RegistryKey<T> | null {
		return this.entries.findKey(value => value === entry) ?? null;
	}

	public getTagEntries(tagKey: TagKey<T>): T[] {
		const entries: Set<T> = new Set();
		const consumer: Consumer<TagKey<T>> = tag => {
			this.tags
				.getOrDefault(tag, [])
				.forEach(value => (value instanceof TagKey
					? consumer(value)
					: entries.add(value)
				));
		};

		consumer(tagKey);

		return Array.from(entries);
	}

	public getRegistryKey(): RegistryKey<Registry<T>> {
		return this.key;
	}

	public getEntryCodec(): BaseValidator<RegistryEntry<T>> {
		return Identifier.getCodec().transform(id => {
			const entry = new RegistryEntry(id, this);

			this.registryEntries.set(Registry.resolveAsKey(this, id), entry);

			return entry;
		});
	}

	public getInstanceEntryCodec(): BaseValidator<T> | null {
		return this.entryCodec?.() ?? null;
	}

	public getTagEntryCodec(): BaseValidator<(TagKey<T> | T)[]> {
		return s.object({
			values: s.union([
				Identifier.getCodec().reshape(id => {
					return this.containsId(id)
						? Result.ok(this.get(id) as T)
						: Result.err<T>(new Error(`Entry ${id} does not exist for the registry ${this}!`));
				}),
				TagKey.codec(this.getRegistryKey())
			]).array()
		}).required().transform(entry => entry.values);
	}

	public size(): number {
		return this.entries.size;
	}

	public omit(id: Identifier): T | null;
	public omit(key: RegistryKey<T>): T | null;
	public omit(idOrKey: Identifier | RegistryKey<T>): T | null {
		const key = Registry.resolveAsKey(this, idOrKey);

		if (!this.contains(key))
			throw new Error(`Attempted to omit undefined key: ${key}!`);

		if (this.getRegistryKey() === RegistryKey.ofRegistry(RegistryKeys.ROOT))
			throw new Error(`Cannot omit value from root registry!`);

		const value = this.get(key);
		this.entries.delete(key);

		this.registryEntries
			.get(key)
			?.validate();

		return value;
	}

	public toString(): string {
		return `Registry["${this.key}"]`;
	}
}

enum State {
	VISITING, VALID, INVALID
}

export default Registry;