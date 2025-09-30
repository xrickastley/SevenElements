package io.github.xrickastley.sevenelements.registry;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class SevenElementsDamageTypeTags {
	public static final TagKey<DamageType> PREVENTS_COOLDOWN_TRIGGER = SevenElementsDamageTypeTags.of("prevents_cooldown_trigger");
	public static final TagKey<DamageType> HAS_PYRO_INFUSION = SevenElementsDamageTypeTags.of("has_pyro_infusion");
	public static final TagKey<DamageType> HAS_HYDRO_INFUSION = SevenElementsDamageTypeTags.of("has_hydro_infusion");
	public static final TagKey<DamageType> HAS_ELECTRO_INFUSION = SevenElementsDamageTypeTags.of("has_electro_infusion");
	public static final TagKey<DamageType> HAS_ANEMO_INFUSION = SevenElementsDamageTypeTags.of("has_anemo_infusion");
	public static final TagKey<DamageType> HAS_DENDRO_INFUSION = SevenElementsDamageTypeTags.of("has_dendro_infusion");
	public static final TagKey<DamageType> HAS_CRYO_INFUSION = SevenElementsDamageTypeTags.of("has_cryo_infusion");
	public static final TagKey<DamageType> HAS_GEO_INFUSION = SevenElementsDamageTypeTags.of("has_geo_infusion");

	private static TagKey<DamageType> of(String path) {
		return TagKey.of(RegistryKeys.DAMAGE_TYPE, SevenElements.identifier(path));
	}
}
