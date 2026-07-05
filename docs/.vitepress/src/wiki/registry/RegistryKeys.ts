
import EntityAttribute from "../objects/EntityAttribute";
import Item from "../objects/Item";

import Identifier from "../util/Identifier";
import Registry from "./Registry";

import RegistryKey from "./RegistryKey";

namespace RegistryKeys {
	export const ROOT: Identifier = Identifier.of("root");
	export const ITEM: RegistryKey<Registry<Item>> = RegistryKey.ofRegistry<Item>(Identifier.of("item"));
	export const ATTRIBUTE: RegistryKey<Registry<EntityAttribute>> = RegistryKey.ofRegistry<EntityAttribute>(Identifier.of("attribute"));
}

export default RegistryKeys;