
import { BaseValidator, Result, s } from "@sapphire/shapeshift";

import Registry from "./Registry";
import RegistryKey from "./RegistryKey";

import ExtendedCollection from "../util/ExtendedCollection";
import Identifier from "../util/Identifier";
import Pair from "../util/Pair";
import Util from "../util/Util";

class TagKey<T> {
	private constructor(registry: RegistryKey<Registry<T>>, value: Identifier) {
		this.registry = registry;
		this.value = value;
	}

	static #INSTANCES: ExtendedCollection<Pair<RegistryKey<Registry<unknown>>, Identifier>, TagKey<any>> = new ExtendedCollection();

	private readonly registry: RegistryKey<Registry<T>>;
	private readonly value: Identifier;

	public static of<T>(registry: RegistryKey<Registry<T>>, id: Identifier): TagKey<T> {
		return this.#INSTANCES
			.computeIfAbsent(
				new Pair(registry, id),
				pair => new TagKey(pair.getLeft(), pair.getRight())
			);
	}

	public static unprefixedCodec<T>(registryKey: RegistryKey<Registry<T>>): BaseValidator<TagKey<T>> {
		return Identifier.getCodec().transform(id => TagKey.of(registryKey, id));
	}

	public static codec<T>(registryKey: RegistryKey<Registry<T>>): BaseValidator<TagKey<T>> {
		return s.string()
			.reshape(string => (string.startsWith("#")
				? Util.asDataResult(() => TagKey.of(registryKey, Identifier.resolve(string.substring(1))))
				: Result.err(new Error("Not a tag id"))
			));
	}

	public getId() {
		return this.value;
	}

	public isOf<T extends Registry<any>>(registry: RegistryKey<T>): boolean {
		return this.registry === registry;
	}

	public toString(): string {
		return `TagKey["${this.registry.getValue()}/${this.value}"]`;
	}
}

export default TagKey;