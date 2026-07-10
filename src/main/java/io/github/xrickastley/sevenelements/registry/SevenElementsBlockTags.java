package io.github.xrickastley.sevenelements.registry;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class SevenElementsBlockTags {
	public static final TagKey<Block> PROGRESSES_DENDRO_ATTUNEMENT = SevenElementsBlockTags.of("progresses_dendro_attunement");
	public static final TagKey<Block> PROGRESSES_GEO_ATTUNEMENT = SevenElementsBlockTags.of("progresses_geo_attunement");

	private static TagKey<Block> of(String id) {
		return TagKey.of(RegistryKeys.BLOCK, SevenElements.identifier(id));
	}
}
