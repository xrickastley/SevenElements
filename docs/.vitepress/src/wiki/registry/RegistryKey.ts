
import Registry from "./Registry";

import ExtendedCollection from "../util/ExtendedCollection";
import Identifier from "../util/Identifier";
import Pair from "../util/Pair";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
class RegistryKey<T> {
	private constructor(registry: Identifier, value: Identifier) {
		this.registry = registry;
		this.value = value;
	}

	static #INSTANCES: ExtendedCollection<Pair<Identifier, Identifier>, RegistryKey<any>> = new ExtendedCollection();

	private readonly registry: Identifier;
	private readonly value: Identifier;

	public static of<T>(registry: RegistryKey<Registry<T>>, value: Identifier): RegistryKey<T> {
		return RegistryKey.ofImpl(registry.value, value);
	}

	public static ofRegistry<T>(registry: Identifier): RegistryKey<Registry<T>> {
		return RegistryKey
			.ofImpl(Identifier.of("root"), registry);
	}

	private static ofImpl<T>(registry: Identifier, value: Identifier): RegistryKey<T> {
		return this.#INSTANCES
			.computeIfAbsent(
				new Pair(registry, value),
				pair => new RegistryKey(pair.getLeft(), pair.getRight())
			);
	}

	public toString(): string {
		return `ResourceKey["${this.registry}/${this.value}"]`;
	}

	public isOf<T extends Registry<unknown>>(registry: RegistryKey<T>): boolean {
		return this.registry.equals(registry.value);
	}

	public getValue(): Identifier {
		return this.value;
	}

	public getRegistry(): Identifier {
		return this.registry;
	}
}

export default RegistryKey;