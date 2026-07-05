
import Identifier from "../util/Identifier";
import Registry from "./Registry";
import RegistryKey from "./RegistryKey";

class RegistryEntry<T> {
	public constructor(id: Identifier, registry: Registry<T>) {
		this.key = RegistryKey.of(registry.getRegistryKey(), id);
		this.registry = registry;
		this.dependents = [];
	}

	private readonly key: RegistryKey<T>;
	private readonly registry: Registry<T>;
	private readonly dependents: RegistryEntry.Dependent<unknown>[];

	public value(): T {
		if (!this.registry.contains(this.key))
			throw new Error(`Registry ${this.registry} does not contain entry: ${this.key}!`);

		return this.registry.get(this.key) as T;
	}

	public getKey(): RegistryKey<T> {
		return this.key;
	}

	public hasEntry(): boolean {
		return this.registry.contains(this.key);
	}

	public validate(): boolean {
		if (this.hasEntry())
			return true;

		this.dependents.forEach(dependent => dependent.omit(this.key));

		return false;
	}

	public addDependent<T>(entry: T, registry: Registry<T>): void {
		this.dependents.push(
			new RegistryEntry.Dependent(entry, registry)
		);
	}
}

namespace RegistryEntry {
	export class Dependent<T> {
		public constructor(entry: T, registry: Registry<T>) {
			this.entry = entry;
			this.registry = registry;
		}

		private readonly entry: T;
		private readonly registry: Registry<T>;

		public omit(refBy: RegistryKey<unknown>) {
			const entryId = this.registry.getId(this.entry) as Identifier;

			console.warn(`Omitting entry: ${entryId} from registry: ${this.registry} due to invalid reference: ${refBy}`);

			this.registry.omit(entryId);
		}
	}
}

export default RegistryEntry;