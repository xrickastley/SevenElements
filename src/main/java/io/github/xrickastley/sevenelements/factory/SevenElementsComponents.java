package io.github.xrickastley.sevenelements.factory;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementalAttunementComponent;
import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public class SevenElementsComponents {
	public static final DataComponentType<ElementalAttunementComponent> ELEMENTAL_ATTUNEMENT_COMPONENT = DataComponentType.<ElementalAttunementComponent>builder().persistent(ElementalAttunementComponent.CODEC).build();
	public static final DataComponentType<ElementalInfusionComponent> ELEMENTAL_INFUSION_COMPONENT = DataComponentType.<ElementalInfusionComponent>builder().persistent(ElementalInfusionComponent.CODEC).build();

	public static void register() {
		register("elemental_attunement", ELEMENTAL_ATTUNEMENT_COMPONENT);
		register("elemental_infusion", ELEMENTAL_INFUSION_COMPONENT);
	}

	public static void register(String id, DataComponentType<?> componentType) {
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, SevenElements.identifier(id), componentType);
	}
}
