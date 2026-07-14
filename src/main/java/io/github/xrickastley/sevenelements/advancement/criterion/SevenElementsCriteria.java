package io.github.xrickastley.sevenelements.advancement.criterion;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class SevenElementsCriteria {
	public static final ElementalInfusionCriterion ELEMENTAL_INFUSION = new ElementalInfusionCriterion();
	public static final PerformAttunementCriterion PERFORM_ATTUNEMENT = new PerformAttunementCriterion();
	public static final ReactionTriggeredCriterion REACTION_TRIGGERED = new ReactionTriggeredCriterion();

	public static void register() {
		register("elemental_infusion", ELEMENTAL_INFUSION);
		register("perform_attunement", PERFORM_ATTUNEMENT);
		register("reaction_triggered", REACTION_TRIGGERED);
	}

	private static void register(String id, CriterionTrigger<?> criterion) {
		Registry.register(BuiltInRegistries.TRIGGER_TYPES, SevenElements.identifier(id), criterion);
	}



	static <T> boolean emptyOrEqual(Optional<T> optional, T equalTo) {
		return optional.isEmpty() || optional.get().equals(equalTo);
	}

	static <T, I> boolean emptyOrPasses(Optional<T> optional, BiPredicate<T, I> predicate, I input) {
		return optional.isEmpty() || predicate.test(optional.get(), input);
	}

	static <T> boolean emptyOrPasses(Optional<T> optional, Predicate<T> predicate) {
		return optional.isEmpty() || predicate.test(optional.get());
	}
}
