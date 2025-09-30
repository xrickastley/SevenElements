package io.github.xrickastley.sevenelements.registry;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class SevenElementsEntityTypeTags {
	public static final TagKey<EntityType<?>> DEALS_PYRO_DAMAGE = SevenElementsEntityTypeTags.of("deals_pyro_damage");
	public static final TagKey<EntityType<?>> DEALS_HYDRO_DAMAGE = SevenElementsEntityTypeTags.of("deals_hydro_damage");
	public static final TagKey<EntityType<?>> DEALS_ELECTRO_DAMAGE = SevenElementsEntityTypeTags.of("deals_electro_damage");
	public static final TagKey<EntityType<?>> DEALS_ANEMO_DAMAGE = SevenElementsEntityTypeTags.of("deals_anemo_damage");
	public static final TagKey<EntityType<?>> DEALS_DENDRO_DAMAGE = SevenElementsEntityTypeTags.of("deals_dendro_damage");
	public static final TagKey<EntityType<?>> DEALS_CRYO_DAMAGE = SevenElementsEntityTypeTags.of("deals_cryo_damage");
	public static final TagKey<EntityType<?>> DEALS_GEO_DAMAGE = SevenElementsEntityTypeTags.of("deals_geo_damage");
	public static final TagKey<EntityType<?>> IGNORED_TARGETS = SevenElementsEntityTypeTags.of("ignored_targets");

	private static TagKey<EntityType<?>> of(String path) {
		return TagKey.of(RegistryKeys.ENTITY_TYPE, SevenElements.identifier(path));
	}
}
