package io.github.xrickastley.sevenelements.registry;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class SevenElementsBlockTags {
	public static final TagKey<Block> PROGRESSES_DENDRO_ATTUNEMENT = SevenElementsBlockTags.of("progresses_dendro_attunement");
	public static final TagKey<Block> PROGRESSES_GEO_ATTUNEMENT = SevenElementsBlockTags.of("progresses_geo_attunement");

	private static TagKey<Block> of(String id) {
		return TagKey.create(Registries.BLOCK, SevenElements.identifier(id));
	}
}
