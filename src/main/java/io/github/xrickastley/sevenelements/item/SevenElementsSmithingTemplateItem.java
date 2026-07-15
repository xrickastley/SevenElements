package io.github.xrickastley.sevenelements.item;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.mixin.SmithingTemplateItemAccessor;

import net.minecraft.item.Item;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class SevenElementsSmithingTemplateItem extends SmithingTemplateItem {
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
	private static final Identifier EMPTY_ARMOR_SLOT_HELMET_TEXTURE = SmithingTemplateItemAccessor.getEmptyArmorSlotHelmetTexture();
	private static final Identifier EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE = SmithingTemplateItemAccessor.getEmptyArmorSlotChestplateTexture();
	private static final Identifier EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE = SmithingTemplateItemAccessor.getEmptyArmorSlotLeggingsTexture();
	private static final Identifier EMPTY_ARMOR_SLOT_BOOTS_TEXTURE = SmithingTemplateItemAccessor.getEmptyArmorSlotBootsTexture();
	private static final Identifier EMPTY_SLOT_AXE_TEXTURE = SmithingTemplateItemAccessor.getEmptySlotAxeTexture();
	private static final Identifier EMPTY_SLOT_SWORD_TEXTURE = SmithingTemplateItemAccessor.getEmptySlotSwordTexture();
	private static final Identifier EMPTY_SLOT_SPEAR_TEXTURE = SmithingTemplateItemAccessor.getEmptySlotSpearTexture();
	private static final Identifier EMPTY_SLOT_TRIDENT_TEXTURE = SevenElements.identifier("container/slot/trident");
	private static final Identifier EMPTY_SLOT_MACE_TEXTURE = SevenElements.identifier("container/slot/mace");
	private static final Identifier EMPTY_SLOT_RUNE_TEXTURE = SevenElements.identifier("container/slot/rune");

	private final Text titleText;
	private final Element attunement;

	private SevenElementsSmithingTemplateItem(
		Element attunement,
		Text appliesToText,
		Text ingredientsText,
		Text titleText,
		Text baseSlotDescriptionText,
		Text additionsSlotDescriptionText,
		Settings settings
	) {
		super(
			appliesToText,
			ingredientsText,
			baseSlotDescriptionText,
			additionsSlotDescriptionText,
			SevenElementsSmithingTemplateItem.getElementalAttunementEmptyBaseSlotTextures(),
			SevenElementsSmithingTemplateItem.getElementalAttunementEmptyAdditionsSlotTextures(),
			settings
		);

		this.attunement = attunement;
		this.titleText = titleText;
	}

	public static SevenElementsSmithingTemplateItem of(Element attunement, Item.Settings settings) {
		return new SevenElementsSmithingTemplateItem(
			attunement,
			ELEMENTAL_ATTUNEMENT_APPLIES_TO_TEXT,
			ELEMENTAL_ATTUNEMENT_INGREDIENTS_TEXT,
			Text.translatable(Util.createTranslationKey("elemental_attunement", attunement.getId())).formatted(SmithingTemplateItemAccessor.getTitleFormatting()),
			ELEMENTAL_ATTUNEMENT_BASE_SLOT_DESCRIPTION_TEXT,
			ELEMENTAL_ATTUNEMENT_ADDITIONS_SLOT_DESCRIPTION_TEXT,
			settings
		);
	}

	public Element getElementalAttunement() {
		return this.attunement;
	}

	private static List<Identifier> getElementalAttunementEmptyBaseSlotTextures() {
		return List.of(
			EMPTY_ARMOR_SLOT_HELMET_TEXTURE,
			EMPTY_SLOT_SWORD_TEXTURE,
			EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE,
			EMPTY_SLOT_AXE_TEXTURE,
			EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE,
			EMPTY_SLOT_MACE_TEXTURE,
			EMPTY_ARMOR_SLOT_BOOTS_TEXTURE,
			EMPTY_SLOT_TRIDENT_TEXTURE,
			EMPTY_SLOT_SPEAR_TEXTURE
		);
	}

	private static List<Identifier> getElementalAttunementEmptyAdditionsSlotTextures() {
		return List.of(
			EMPTY_SLOT_RUNE_TEXTURE
		);
	}

	@ApiStatus.Internal
	public Text getTitleText() {
		return this.titleText;
	}
}
