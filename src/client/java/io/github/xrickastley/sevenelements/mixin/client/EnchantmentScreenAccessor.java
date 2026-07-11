package io.github.xrickastley.sevenelements.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.util.Identifier;

@Mixin(EnchantmentScreen.class)
public interface EnchantmentScreenAccessor {
	@Accessor("LEVEL_TEXTURES")
	public static Identifier[] getLevelTextures() { throw new AssertionError(); }

	@Accessor("LEVEL_DISABLED_TEXTURES")
	public static Identifier[] getLevelDisabledTextures() { throw new AssertionError(); }

	@Accessor("ENCHANTMENT_SLOT_DISABLED_TEXTURE")
	public static Identifier getEnchantmentSlotDisabledTexture() { throw new AssertionError(); }

	@Accessor("ENCHANTMENT_SLOT_HIGHLIGHTED_TEXTURE")
	public static Identifier getEnchantmentSlotHighlightedTexture() { throw new AssertionError(); }

	@Accessor("ENCHANTMENT_SLOT_TEXTURE")
	public static Identifier getEnchantmentSlotTexture() { throw new AssertionError(); }
}
