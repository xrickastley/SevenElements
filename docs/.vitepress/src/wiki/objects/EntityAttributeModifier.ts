
import { BaseValidator, Constructor, s } from "@sapphire/shapeshift";
import { sprintf } from "sprintf-js";

import AttributeModifierSlot from "./AttributeModifierSlot";
import EntityAttribute from "./EntityAttribute";
import Item from "./Item";

import Registries from "../registry/Registries";
import RegistryEntry from "../registry/RegistryEntry";

import EnumHelper from "../util/helpers/EnumHelper";

import EnumResolvers from "../util/EnumResolvers";
import Formatting from "../util/Formatting";
import Identifier from "../util/Identifier";

class EntityAttributeModifier {
	private constructor(data: EntityAttributeModifierData) {
		this.id = data.id;
		this.attribute = data.attribute;
		this.slot = data.slot;
		this.amount = data.amount;
		this.operation = data.operation;
	}

	private readonly id: Identifier;
	private readonly attribute: RegistryEntry<EntityAttribute>;
	private readonly slot: AttributeModifierSlot;
	private readonly amount: number;
	private readonly operation: EntityAttributeModifier.Operation;

	private static getOperationCodec(): BaseValidator<EntityAttributeModifier.Operation> {
		return s.enum(EnumHelper.enumValues(EntityAttributeModifier.Operation));
	}

	private static getAttributeModifierSlotCodec(): BaseValidator<AttributeModifierSlot> {
		return s.enum(EnumHelper.enumValues(AttributeModifierSlot));
	}

	public static getCodec(): BaseValidator<EntityAttributeModifier> {
		return s.union([
			s.object({
				id: Identifier.getCodec(),
				attribute: Registries.ATTRIBUTE.getEntryCodec(),
				slot: EntityAttributeModifier.getAttributeModifierSlotCodec(),
				amount: s.number(),
				operation: EntityAttributeModifier.getOperationCodec().default(EntityAttributeModifier.Operation.ADD_VALUE)
			}).transform(value => new EntityAttributeModifier(value)),
			s.instance(EntityAttributeModifier as unknown as Constructor<EntityAttributeModifier>)
		]);
	}

	public getId(): Identifier {
		return this.id;
	}

	public getAttribute(): RegistryEntry<EntityAttribute> {
		return this.attribute;
	}

	public getSlot(): AttributeModifierSlot {
		return this.slot;
	}

	public getAmount(): number {
		return this.amount;
	}

	public getOperation(): EntityAttributeModifier.Operation {
		return this.operation;
	}

	public getModifierTooltip(): string | undefined {
		let value = this.amount;

		if (this.operation === EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE || this.operation === EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
			value *= 100.0;
		} else if (this.attribute.getKey().getValue().equals(Identifier.ofVanilla(`knockback_resistance`))) {
			value *= 10.0;
		}

		const isBase = this.id.equals(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID) || this.id.equals(Item.BASE_ATTACK_SPEED_MODIFIER_ID);

		const format = EnumResolvers.getOperationFormat(this.operation);
		const prefix = isBase
			? "&nbsp;"
			: value > 0
				? "+"
				: "-";

		const formatting = isBase
			? Formatting.DARK_GREEN
			: this.attribute.value().getFormatting(value > 0);

		return `<span class="${formatting}">${prefix}${sprintf(format, Math.abs(value), this.attribute.value().getName())}</span>`;
	}
}

interface EntityAttributeModifierData {
	id: Identifier;
	attribute: RegistryEntry<EntityAttribute>;
	slot: AttributeModifierSlot;
	amount: number;
	operation: EntityAttributeModifier.Operation;
}

namespace EntityAttributeModifier {
	export enum Operation {
		ADD_VALUE = "ADD_VALUE",
		ADD_MULTIPLIED_BASE = "ADD_MULTIPLIED_BASE",
		ADD_MULTIPLIED_TOTAL = "ADD_MULTIPLIED_TOTAL"
	}
}

export default EntityAttributeModifier;