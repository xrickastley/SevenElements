package io.github.xrickastley.sevenelements.factory;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementalAttunementComponent;
import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SevenElementsComponents {
	public static final ComponentType<ElementalAttunementComponent> ELEMENTAL_ATTUNEMENT_COMPONENT = ComponentType.<ElementalAttunementComponent>builder().codec(ElementalAttunementComponent.CODEC).build();
	public static final ComponentType<ElementalInfusionComponent> ELEMENTAL_INFUSION_COMPONENT = ComponentType.<ElementalInfusionComponent>builder().codec(ElementalInfusionComponent.CODEC).build();

	public static void register() {
		register("elemental_attunement", ELEMENTAL_ATTUNEMENT_COMPONENT);
		register("elemental_infusion", ELEMENTAL_INFUSION_COMPONENT);
	}

	public static void register(String id, ComponentType<?> componentType) {
		Registry.register(Registries.DATA_COMPONENT_TYPE, SevenElements.identifier(id), componentType);
	}
}
