package io.github.xrickastley.sevenelements.advancement.criterion;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class SevenElementsCriteria {
	public static final ElementalInfusionCriterion ELEMENTAL_INFUSION = new ElementalInfusionCriterion();

	public static void register() {
		register("elemental_infusion", ELEMENTAL_INFUSION);
	}

	private static void register(String id, CriterionTrigger<?> criterion) {
		Registry.register(BuiltInRegistries.TRIGGER_TYPES, SevenElements.identifier(id), criterion);
	}
}
