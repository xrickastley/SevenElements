package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@Mixin(SmithingTemplateItem.class)
public interface SmithingTemplateItemAccessor {
	@Accessor("TITLE_FORMATTING")
	public static Formatting getTitleFormatting() { throw new AssertionError(); }
	@Accessor("DESCRIPTION_FORMATTING")
	public static Formatting getDescriptionFormatting() { throw new AssertionError(); }
	@Accessor("SMITHING_TEMPLATE_TEXT")
	public static Text getSmithingTemplateText() { throw new AssertionError(); }
	@Accessor("EMPTY_ARMOR_SLOT_HELMET_TEXTURE")
	public static Identifier getEmptyArmorSlotHelmetTexture() { throw new AssertionError(); }
	@Accessor("EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE")
	public static Identifier getEmptyArmorSlotChestplateTexture() { throw new AssertionError(); }
	@Accessor("EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE")
	public static Identifier getEmptyArmorSlotLeggingsTexture() { throw new AssertionError(); }
	@Accessor("EMPTY_ARMOR_SLOT_BOOTS_TEXTURE")
	public static Identifier getEmptyArmorSlotBootsTexture() { throw new AssertionError(); }
	@Accessor("EMPTY_SLOT_AXE_TEXTURE")
	public static Identifier getEmptySlotAxeTexture() { throw new AssertionError(); }
	@Accessor("EMPTY_SLOT_SWORD_TEXTURE")
	public static Identifier getEmptySlotSwordTexture() { throw new AssertionError(); }
	@Accessor("EMPTY_SLOT_SPEAR_TEXTURE")
	public static Identifier getEmptySlotSpearTexture() { throw new AssertionError(); }
}
