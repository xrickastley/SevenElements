package io.github.xrickastley.sevenelements.registry;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class SevenElementsDamageTypes {
	public static final RegistryKey<DamageType> BURNING = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, SevenElements.identifier("burning"));
	public static final RegistryKey<DamageType> DENDRO_CORE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, SevenElements.identifier("dendro_core"));
	public static final RegistryKey<DamageType> ELECTRO_CHARGED = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, SevenElements.identifier("electro-charged"));
	public static final RegistryKey<DamageType> OVERLOADED = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, SevenElements.identifier("overloaded"));
	public static final RegistryKey<DamageType> SHATTER = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, SevenElements.identifier("shatter"));
	public static final RegistryKey<DamageType> SUPERCONDUCT = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, SevenElements.identifier("superconduct"));
	public static final RegistryKey<DamageType> SWIRL = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, SevenElements.identifier("swirl"));
}
