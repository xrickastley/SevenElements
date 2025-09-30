package io.github.xrickastley.sevenelements.advancement.criterion;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SevenElementsCriteria {
	public static final ElementalInfusionCriterion ELEMENTAL_INFUSION = new ElementalInfusionCriterion();

	public static void register() {
		register("elemental_infusion", ELEMENTAL_INFUSION);
	}

	private static void register(String id, Criterion<?> criterion) {
		Registry.register(Registries.CRITERION, SevenElements.identifier(id), criterion);
	}
}
