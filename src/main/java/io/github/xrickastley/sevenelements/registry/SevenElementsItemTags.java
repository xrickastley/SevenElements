package io.github.xrickastley.sevenelements.registry;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class SevenElementsItemTags {
   public static final TagKey<Item> HEAVY_WEAPON = SevenElementsItemTags.of("heavy_weapon");

	private static TagKey<Item> of(String id) {
		return TagKey.of(RegistryKeys.ITEM, SevenElements.identifier(id));
	}
}
