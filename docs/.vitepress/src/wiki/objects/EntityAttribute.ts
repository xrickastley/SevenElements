
import { BaseValidator, Constructor, s } from "@sapphire/shapeshift";
import EnumHelper from "../util/helpers/EnumHelper";
import Formatting from "../util/Formatting";

class EntityAttribute {
	private constructor(data: EntityAttributeData) {
		this.name = data.name;
		this.category = data.category;
	}

	private readonly name: string;
	private readonly category: EntityAttribute.Category;

	public static getCategoryCodec(): BaseValidator<EntityAttribute.Category> {
		return s.enum(EnumHelper.enumValues(EntityAttribute.Category));
	}

	public static getCodec(): BaseValidator<EntityAttribute> {
		return s.union([
			s.object({
				name: s.string().lengthGreaterThan(1),
				category: EntityAttribute.getCategoryCodec().default(EntityAttribute.Category.POSITIVE)
			}).transform(value => new EntityAttribute(value)),
			s.instance(EntityAttribute as unknown as Constructor<EntityAttribute>)
		]);
	}

	public getName(): string {
		return this.name;
	}

	public getCategory(): EntityAttribute.Category {
		return this.category;
	}

	public getFormatting(positive: boolean) {
		switch (this.category) {
			case EntityAttribute.Category.POSITIVE:
				return positive ? Formatting.BLUE : Formatting.RED;
			case EntityAttribute.Category.NEUTRAL:
				return Formatting.GRAY;
			case EntityAttribute.Category.NEGATIVE:
				return positive ? Formatting.RED : Formatting.BLUE;
			default:
				throw new Error(`Invalid attribute category: ${this.category}`);
		}
	}
}

interface EntityAttributeData {
	name: string;
	category: EntityAttribute.Category;
}

namespace EntityAttribute {
	export enum Category {
		POSITIVE = "POSITIVE",
		NEUTRAL = "NEUTRAL",
		NEGATIVE = "NEGATIVE"
	}
}

export default EntityAttribute;