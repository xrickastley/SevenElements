package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.SmithingTemplateItem;

@Mixin(SmithingTemplateItem.class)
public interface SmithingTemplateItemAccessor {
	@Accessor("TITLE_FORMAT")
	public static ChatFormatting getTitleFormatting() { throw new AssertionError(); }
	@Accessor("DESCRIPTION_FORMAT")
	public static ChatFormatting getDescriptionFormatting() { throw new AssertionError(); }
	@Accessor("SMITHING_TEMPLATE_SUFFIX")
	public static Component getSmithingTemplateText() { throw new AssertionError(); }
	@Accessor("EMPTY_SLOT_HELMET")
	public static Identifier getEmptyArmorSlotHelmetTexture() { throw new AssertionError(); }
	@Accessor("EMPTY_SLOT_CHESTPLATE")
	public static Identifier getEmptyArmorSlotChestplateTexture() { throw new AssertionError(); }
	@Accessor("EMPTY_SLOT_LEGGINGS")
	public static Identifier getEmptyArmorSlotLeggingsTexture() { throw new AssertionError(); }
	@Accessor("EMPTY_SLOT_BOOTS")
	public static Identifier getEmptyArmorSlotBootsTexture() { throw new AssertionError(); }
	@Accessor("EMPTY_SLOT_AXE")
	public static Identifier getEmptySlotAxeTexture() { throw new AssertionError(); }
	@Accessor("EMPTY_SLOT_SWORD")
	public static Identifier getEmptySlotSwordTexture() { throw new AssertionError(); }
	@Accessor("EMPTY_SLOT_SPEAR")
	public static Identifier getEmptySlotSpearTexture() { throw new AssertionError(); }
}
