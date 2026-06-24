package io.github.xrickastley.sevenelements.item;

import java.util.List;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.mixin.SmithingTemplateItemAccessor;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class SevenElementsSmithingTemplateItem extends SmithingTemplateItem {
	private static final Identifier EMPTY_SLOT_RUNE_TEXTURE = SevenElements.identifier("item/empty_slot_rune");

	private static final Text ELEMENTAL_ATTUNEMENT_APPLIES_TO_TEXT = Text.translatable(
			Util.createTranslationKey("item", SevenElements.identifier("smithing_template.elemental_attunement.applies_to"))
		)
		.formatted(SmithingTemplateItemAccessor.getDescriptionFormatting());
	private static final Text ELEMENTAL_ATTUNEMENT_INGREDIENTS_TEXT = Text.translatable(
			Util.createTranslationKey("item", SevenElements.identifier("smithing_template.elemental_attunement.ingredients"))
		)
		.formatted(SmithingTemplateItemAccessor.getDescriptionFormatting());
	private static final Text ELEMENTAL_ATTUNEMENT_BASE_SLOT_DESCRIPTION_TEXT = Text.translatable(
		Util.createTranslationKey("item", SevenElements.identifier("smithing_template.elemental_attunement.base_slot_description"))
	);
	private static final Text ELEMENTAL_ATTUNEMENT_ADDITIONS_SLOT_DESCRIPTION_TEXT = Text.translatable(
		Util.createTranslationKey("item", SevenElements.identifier("smithing_template.elemental_attunement.additions_slot_description"))
	);

	private final Element attunement;

	private SevenElementsSmithingTemplateItem(
		Element attunement,
		Text appliesToText,
		Text ingredientsText,
		Text titleText,
		Text baseSlotDescriptionText,
		Text additionsSlotDescriptionText,
		FeatureFlag... requiredFeatures
	) {
		super(
			appliesToText,
			ingredientsText,
			titleText,
			baseSlotDescriptionText,
			additionsSlotDescriptionText,
			SmithingTemplateItemAccessor.sevenelements$invokeGetNetheriteUpgradeEmptyBaseSlotTextures(),
			List.of(EMPTY_SLOT_RUNE_TEXTURE),
			requiredFeatures
		);

		this.attunement = attunement;
	}

	public static SevenElementsSmithingTemplateItem of(Element attunement, FeatureFlag... requiredFeatures) {
		return new SevenElementsSmithingTemplateItem(
			attunement,
			ELEMENTAL_ATTUNEMENT_APPLIES_TO_TEXT,
			ELEMENTAL_ATTUNEMENT_INGREDIENTS_TEXT,
			Text.translatable(Util.createTranslationKey("elemental_attunement", attunement.getId())).formatted(SmithingTemplateItemAccessor.getTitleFormatting()),
			ELEMENTAL_ATTUNEMENT_BASE_SLOT_DESCRIPTION_TEXT,
			ELEMENTAL_ATTUNEMENT_ADDITIONS_SLOT_DESCRIPTION_TEXT,
			requiredFeatures
		);
	}

	public Element getElementalAttunement() {
		return this.attunement;
	}
}
