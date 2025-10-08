package io.github.xrickastley.sevenelements.advancement.criterion;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;

public class SevenElementsCriteria {
	public static final ElementalInfusionCriterion ELEMENTAL_INFUSION = new ElementalInfusionCriterion();

	public static void register() {
		register(SevenElementsCriteria.ELEMENTAL_INFUSION);
	}

	private static void register(Criterion<?> criterion) {
		Criteria.register(criterion);
	}
}
