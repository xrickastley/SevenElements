
import { BaseValidator, Constructor, s } from "@sapphire/shapeshift";

import EntityAttributeModifier from "./EntityAttributeModifier";

import Registries from "../registry/Registries";
import TextureRegistry from "../registry/TextureRegistry";

import EnumHelper from "../util/helpers/EnumHelper";

import Collectors from "../util/Collectors";
import EnumResolvers from "../util/EnumResolvers";
import Formatting from "../util/Formatting";
import Identifier from "../util/Identifier";
import Stream from "../util/Stream";
import Util from "../util/Util";

class Item {
	private constructor(data: ItemData) {
		this.name = data.name;
		this.link = data.link;
		this.icon = data.icon instanceof Identifier
			? TextureRegistry.getTexture(data.icon)
			: data.icon;
		this.invIcon = data.inventory_icon instanceof Identifier
			? TextureRegistry.getTexture(data.inventory_icon)
			: data.inventory_icon ?? this.icon;
		this.rarity = data.rarity;
		this.attributeModifiers = data.attribute_modifiers;
		this.lore = data.lore;

		this.attributeModifiers
			.forEach(modifier => modifier.getAttribute().addDependent(this, Registries.ITEM));
	}

	private readonly name: string;
	private readonly link: string;
	private readonly icon: string;
	private readonly invIcon: string;
	private readonly rarity: Item.Rarity;
	private readonly attributeModifiers: EntityAttributeModifier[];
	private readonly lore: string[];

	public static readonly BASE_ATTACK_DAMAGE_MODIFIER_ID = Identifier.ofVanilla("base_attack_damage");
	public static readonly BASE_ATTACK_SPEED_MODIFIER_ID = Identifier.ofVanilla("base_attack_speed");

	private static getRarityCodec(): BaseValidator<Item.Rarity> {
		return s.enum(EnumHelper.enumValues(Item.Rarity));
	}

	public static getCodec(): BaseValidator<Item> {
		return s.union([
			s.object({
				name: s.string().lengthGreaterThan(1),
				link: s.union([s.string().url(), s.string().transform(string => (!string.startsWith(import.meta.env.BASE_URL) ? import.meta.env.BASE_URL + string : string))]),
				icon: s.union([Identifier.getCodec(), s.string().url()]),
				inventory_icon: s.union([Identifier.getCodec(), s.string().url()]).optional(),
				rarity: Item.getRarityCodec().default(Item.Rarity.COMMON),
				attribute_modifiers: EntityAttributeModifier.getCodec().array().default([]),
				lore: s.string().array().default([])
			}).transform(value => new Item(value)),
			s.instance(Item as unknown as Constructor<Item>)
		]);
	}

	public static of(json: unknown): Item {
		return Item.getCodec().parse(json);
	}

	public getName(): string {
		return this.name;
	}

	public getLink(): string {
		return this.link;
	}

	public getIcon(): string {
		return this.icon;
	}

	public getInventoryIcon(): string {
		return this.invIcon;
	}

	public getRarity(): Item.Rarity {
		return this.rarity;
	}

	public getTooltip(): string {
		const tooltip: string[] = [];

		tooltip.push(`<span class="${EnumResolvers.getRarityFormat(this.getRarity())}">${this.getName()}</span>`);
		this.lore.forEach(line => tooltip.push(Util.formatText(line)));
		this.pushAttributeModifiers(tooltip);

		return tooltip.join("<br>");
	}

	private pushAttributeModifiers(tooltip: string[]) {
		if (!this.attributeModifiers.length)
			return;

		const perSlotModifiers = Stream.of(this.attributeModifiers)
			.collect(Collectors.keyed(modifier => modifier.getSlot()));

		for (const [slot, modifiers] of perSlotModifiers.entries()) {
			tooltip.push("");
			tooltip.push(`<span class="${Formatting.GRAY}">${EnumResolvers.getSlotText(slot)}</span>`);

			for (const modifier of modifiers) {
				const tooltipText = modifier.getModifierTooltip();

				if (tooltipText) tooltip.push(tooltipText);
			}
		}
	}
}

interface ItemData {
	name: string;
	link: string;
	icon: string | Identifier;
	inventory_icon?: string | Identifier | null;
	rarity: Item.Rarity;
	attribute_modifiers: EntityAttributeModifier[];
	lore: string[];
}

namespace Item {
	export enum Rarity {
		COMMON = "COMMON",
		UNCOMMON = "UNCOMMON",
		RARE = "RARE",
		EPIC = "EPIC"
	}
}

export default Item;