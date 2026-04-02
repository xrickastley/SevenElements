package io.github.xrickastley.sevenelements.registry;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class SevenElementsDamageTypes {
	public static final ResourceKey<DamageType> BURNING = ResourceKey.create(Registries.DAMAGE_TYPE, SevenElements.identifier("burning"));
	public static final ResourceKey<DamageType> DENDRO_CORE = ResourceKey.create(Registries.DAMAGE_TYPE, SevenElements.identifier("dendro_core"));
	public static final ResourceKey<DamageType> ELECTRO_CHARGED = ResourceKey.create(Registries.DAMAGE_TYPE, SevenElements.identifier("electro-charged"));
	public static final ResourceKey<DamageType> OVERLOADED = ResourceKey.create(Registries.DAMAGE_TYPE, SevenElements.identifier("overloaded"));
	public static final ResourceKey<DamageType> SHATTER = ResourceKey.create(Registries.DAMAGE_TYPE, SevenElements.identifier("shatter"));
	public static final ResourceKey<DamageType> SUPERCONDUCT = ResourceKey.create(Registries.DAMAGE_TYPE, SevenElements.identifier("superconduct"));
	public static final ResourceKey<DamageType> SWIRL = ResourceKey.create(Registries.DAMAGE_TYPE, SevenElements.identifier("swirl"));
}
