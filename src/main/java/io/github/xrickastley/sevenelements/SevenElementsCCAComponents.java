package io.github.xrickastley.sevenelements;

import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.item.ItemComponentInitializer;
import org.ladysnake.cca.api.v3.item.ItemComponentMigrationRegistry;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.component.ElementComponentImpl;
import io.github.xrickastley.sevenelements.component.FrozenEffectComponent;
import io.github.xrickastley.sevenelements.component.FrozenEffectComponentImpl;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;

import net.minecraft.entity.LivingEntity;


public class SevenElementsCCAComponents implements EntityComponentInitializer, ItemComponentInitializer {
	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerFor(LivingEntity.class, ElementComponent.KEY, ElementComponentImpl::new);
		registry.registerFor(LivingEntity.class, FrozenEffectComponent.KEY, FrozenEffectComponentImpl::new);
	}

	@Override
	public void registerItemComponentMigrations(ItemComponentMigrationRegistry registry) {
		registry.registerMigration(SevenElements.identifier("elemental_infusions"), SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);
	}
}
