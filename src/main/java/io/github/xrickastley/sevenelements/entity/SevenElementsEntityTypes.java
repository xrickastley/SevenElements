package io.github.xrickastley.sevenelements.entity;

import java.util.function.Supplier;

import io.github.xrickastley.sevenelements.SevenElements;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class SevenElementsEntityTypes {
	public static final EntityType<DendroCoreEntity> DENDRO_CORE = EntityType.Builder
		.<DendroCoreEntity>of(DendroCoreEntity::new, MobCategory.MISC)
		.sized(0.3125f, 0.4296875f)
		.clientTrackingRange(64)
		.build(SevenElements.registryKey(Registries.ENTITY_TYPE, "dendro_core"));

	public static final EntityType<CrystallizeShardEntity> CRYSTALLIZE_SHARD = EntityType.Builder
		.<CrystallizeShardEntity>of(CrystallizeShardEntity::new, MobCategory.MISC)
		.sized(0.3125f, 0.875f)
		.clientTrackingRange(64)
		.build(SevenElements.registryKey(Registries.ENTITY_TYPE, "crystallize_shard"));

	public static void register() {
		register("dendro_core", SevenElementsEntityTypes.DENDRO_CORE, SevenElementsEntity::getAttributeBuilder);
		register("crystallize_shard", SevenElementsEntityTypes.CRYSTALLIZE_SHARD, SevenElementsEntity::getAttributeBuilder);
	}

	private static <T extends LivingEntity> void register(String id, EntityType<T> entityType, Supplier<AttributeSupplier.Builder> builderSupplier) {
		register(id, entityType, builderSupplier.get());
	}

	private static <T extends LivingEntity> void register(String id, EntityType<T> entityType, AttributeSupplier.Builder builder) {
		FabricDefaultAttributeRegistry.register(entityType, builder);

		Registry.register(BuiltInRegistries.ENTITY_TYPE, SevenElements.identifier(id), entityType);
	}
}
