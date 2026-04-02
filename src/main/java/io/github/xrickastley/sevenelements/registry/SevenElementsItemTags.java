package io.github.xrickastley.sevenelements.registry;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class SevenElementsItemTags {
	public static final TagKey<Item> HEAVY_WEAPON = SevenElementsItemTags.of("heavy_weapon");

	private static TagKey<Item> of(String id) {
		return TagKey.create(Registries.ITEM, SevenElements.identifier(id));
	}
}
