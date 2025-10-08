package io.github.xrickastley.sevenelements;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.component.ElementComponentImpl;
import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.component.FrozenEffectComponent;
import io.github.xrickastley.sevenelements.component.FrozenEffectComponentImpl;

import net.minecraft.entity.LivingEntity;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;


public class SevenElementsCCAComponents implements EntityComponentInitializer, ItemComponentInitializer {
	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerFor(LivingEntity.class, ElementComponent.KEY, ElementComponentImpl::new);
		registry.registerFor(LivingEntity.class, FrozenEffectComponent.KEY, FrozenEffectComponentImpl::new);
	}

	@Override
	public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
		registry.register(i -> true, ElementalInfusionComponent.KEY, ElementalInfusionComponent::new);
	}
}
