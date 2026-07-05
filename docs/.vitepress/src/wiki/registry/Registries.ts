
import { BaseError } from "@sapphire/shapeshift";

import EntityAttribute from "../objects/EntityAttribute";
import Item from "../objects/Item";

import Registry from "./Registry";
import RegistryKey from "./RegistryKey";
import RegistryKeys from "./RegistryKeys";
import TagKey from "./TagKey";

import Identifier from "../util/Identifier";
import Pair from "../util/Pair";
import Util from "../util/Util";

namespace Registries {
	export const ROOT: Registry<Registry<unknown>> = new Registry(RegistryKey.ofRegistry(RegistryKeys.ROOT));
	export const ATTRIBUTE = new Registry(RegistryKeys.ATTRIBUTE, EntityAttribute.getCodec);
	export const ITEM = new Registry(RegistryKeys.ITEM, Item.getCodec);

	export function bootstrap(): void {
		const dataEntries = Object.entries(import.meta.glob("@data/**/*.json", { eager: true, import: "default", query: "?raw" }))
			.map<[string, Pair<any, null> | Pair<null, unknown>]>(entry => [entry[0], Util.tryOrCatch(() => JSON.parse(entry[1] as string))])
			.filter(entry => {
				if (!entry[1].getLeft())
					console.error(`An error occured while parsing file: ${entry[0]}`, entry[1].getRight());

				return Boolean(entry[1].getLeft());
			})
			.map<[string, any]>(entry => [entry[0], entry[1].getLeft()]);

		const tagEntries: [string, string, unknown][] = [];

		for (const dataEntry of dataEntries) {
			const [entryPath, data] = dataEntry;
			const parts = entryPath.slice(entryPath.indexOf("data/") + 5).replace(/\\/g, "/").split("/");

			if (parts.length < 3) continue;

			const namespace = parts.shift() as string;
			const registry = parts.shift() as string;
			const path = parts.join("/");

			if (registry === "tags")
				tagEntries.push([namespace, path, data]);
			else {
				registerEntry(namespace, registry, path, data);
			}
		}

		registerTags(tagEntries);

		console.warn("Freezing all registries!");

		Registries.ROOT.forEach(registry => registry.freeze());
	}

	function registerEntry(namespace: string, registryPath: string, path: string, data: unknown): void {
		const registryId = Identifier.of(registryPath);

		if (!Registries.ROOT.containsId(registryId))
			return void console.error(`Root registry does not contain registry id: ${registryId}`);

		const idResult = Util.tryOrCatch(() => Identifier.of(namespace, path.slice(0, path.lastIndexOf("."))));

		if (idResult.getRight())
			return void console.error(`Invalid id for entry: "${namespace}:${path}", ignoring!\n`, idResult.getRight());

		const entryId = idResult.getLeft() as Identifier;
		const registry = Registries.ROOT.getOrThrow(registryId);
		const entryCodec = registry.getInstanceEntryCodec();

		if (!entryCodec)
			return void Registry.register(registry, entryId, data);

		const dataResult = entryCodec.run(data);

		if (!dataResult.success)
			return void console.error(`An error occured while parsing entry: ${entryId} for registry: ${registryId}!`, Util.unpackNestedErrors(dataResult.error as BaseError));

		Registry.register(registry, entryId, dataResult.value);
	}

	function registerTags(tagEntries: [string, string, unknown][]): void {
		for (const tagEntry of tagEntries) {
			const [namespace, path, data] = tagEntry;
			const parts = path.split("/");

			if (parts.length < 2) continue;

			const tagRegistry = parts.shift() as string;
			const tagPath = parts.join("/");

			registerTag(namespace, tagRegistry, tagPath, data);
		}
	}

	function registerTag(namespace: string, tagRegistry: string, path: string, data: unknown): void {
		const registryId = Identifier.of(tagRegistry);

		if (!Registries.ROOT.containsId(registryId))
			return;

		const idResult = Util.tryOrCatch(() => Identifier.of(namespace, path.slice(0, path.lastIndexOf("."))));

		if (idResult.getRight())
			return void console.error(`Invalid id for tag entry: "${namespace}:${path}", ignoring!\n`, idResult.getRight());

		const tagEntryId = idResult.getLeft() as Identifier;
		const registry = Registries.ROOT.getOrThrow(registryId);
		const tagId = TagKey.of(registry.getRegistryKey(), tagEntryId);

		const dataResult = registry.getTagEntryCodec().run(data);

		if (!dataResult.value)
			return void console.error(`An error occured while parsing tag: ${tagId} for registry: ${registryId}!`, dataResult.error);

		Registry.registerTag(registry, tagId, dataResult.value);
	}
}

export default Registries;

Registries.bootstrap();