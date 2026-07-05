
import AttributeModifierSlot from "../objects/AttributeModifierSlot";
import EntityAttributeModifier from "../objects/EntityAttributeModifier";
import Item from "../objects/Item";

import Formatting from "./Formatting";

namespace EnumResolvers {
	export function getSlotText(slot: AttributeModifierSlot) {
		switch (slot) {
			case AttributeModifierSlot.ANY:
			case AttributeModifierSlot.BODY:
				return `When equipped:`;
			case AttributeModifierSlot.MAINHAND: return `When in Main Hand:`;
			case AttributeModifierSlot.OFFHAND: return `When in Off Hand:`;
			case AttributeModifierSlot.HAND: return `When held:`;
			case AttributeModifierSlot.FEET: return `When on Feet:`;
			case AttributeModifierSlot.LEGS: return `When on Legs:`;
			case AttributeModifierSlot.CHEST: return `When on Chest:`;
			case AttributeModifierSlot.HEAD: return `When on Head:`;
			case AttributeModifierSlot.ARMOR: return `When equipped:`;
			default:
				throw new Error(`Invalid attribute modifier slot: ${slot}`);
		}
	}

	export function getOperationFormat(operation: EntityAttributeModifier.Operation) {
		switch (operation) {
			case EntityAttributeModifier.Operation.ADD_VALUE:
				return `%s %s`;
			case EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE:
			case EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL:
				return `%s%% %s`;
			default:
				throw new Error(`Invalid attribute modifier operation: ${operation}`);
		}
	}

	export function getRarityFormat(rarity: Item.Rarity) {
		switch (rarity) {
			case Item.Rarity.COMMON: return Formatting.WHITE;
			case Item.Rarity.UNCOMMON: return Formatting.YELLOW;
			case Item.Rarity.RARE: return Formatting.AQUA;
			case Item.Rarity.EPIC: return Formatting.LIGHT_PURPLE;
			default:
				throw new Error(`Invalid item rarity: ${rarity}`);
		}
	}
}

export default EnumResolvers;