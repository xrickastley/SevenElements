package io.github.xrickastley.sevenelements.entity;

import java.util.function.Supplier;

import io.github.xrickastley.sevenelements.SevenElements;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SevenElementsEntityTypes {
	public static final EntityType<DendroCoreEntity> DENDRO_CORE = FabricEntityTypeBuilder
		.<DendroCoreEntity>create(SpawnGroup.MISC, DendroCoreEntity::new)
		.dimensions(EntityDimensions.fixed(0.3125f, 0.4296875f))
		.trackRangeBlocks(64)
		.build();

	public static final EntityType<CrystallizeShardEntity> CRYSTALLIZE_SHARD = FabricEntityTypeBuilder
		.<CrystallizeShardEntity>create(SpawnGroup.MISC, CrystallizeShardEntity::new)
		.dimensions(EntityDimensions.fixed(0.3125f, 0.875f))
		.trackRangeBlocks(64)
		.build();

	public static void register() {
		register("dendro_core", SevenElementsEntityTypes.DENDRO_CORE, SevenElementsEntity::getAttributeBuilder);
		register("crystallize_shard", SevenElementsEntityTypes.CRYSTALLIZE_SHARD, SevenElementsEntity::getAttributeBuilder);
	}

	private static <T extends LivingEntity> void register(String id, EntityType<T> entityType, Supplier<DefaultAttributeContainer.Builder> builderSupplier) {
		register(id, entityType, builderSupplier.get());
	}

	private static <T extends LivingEntity> void register(String id, EntityType<T> entityType, DefaultAttributeContainer.Builder builder) {
		FabricDefaultAttributeRegistry.register(entityType, builder);

		Registry.register(Registries.ENTITY_TYPE, SevenElements.identifier(id), entityType);
	}
}
