package io.github.xrickastley.sevenelements.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.resources.Identifier;

@Mixin(EnchantmentScreen.class)
public interface EnchantmentScreenAccessor {
	@Accessor("ENABLED_LEVEL_SPRITES")
	public static Identifier[] getLevelTextures() { throw new AssertionError(); }

	@Accessor("DISABLED_LEVEL_SPRITES")
	public static Identifier[] getLevelDisabledTextures() { throw new AssertionError(); }

	@Accessor("ENCHANTMENT_SLOT_DISABLED_SPRITE")
	public static Identifier getEnchantmentSlotDisabledTexture() { throw new AssertionError(); }

	@Accessor("ENCHANTMENT_SLOT_HIGHLIGHTED_SPRITE")
	public static Identifier getEnchantmentSlotHighlightedTexture() { throw new AssertionError(); }

	@Accessor("ENCHANTMENT_SLOT_SPRITE")
	public static Identifier getEnchantmentSlotTexture() { throw new AssertionError(); }
}
