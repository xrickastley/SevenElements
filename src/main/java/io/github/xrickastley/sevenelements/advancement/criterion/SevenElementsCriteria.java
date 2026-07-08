package io.github.xrickastley.sevenelements.advancement.criterion;

import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;

public class SevenElementsCriteria {
	public static final ElementalInfusionCriterion ELEMENTAL_INFUSION = new ElementalInfusionCriterion();
	public static final PerformAttunementCriterion PERFORM_ATTUNEMENT = new PerformAttunementCriterion();
	public static final ReactionTriggeredCriterion REACTION_TRIGGERED = new ReactionTriggeredCriterion();

	public static void register() {
		register(SevenElementsCriteria.ELEMENTAL_INFUSION);
		register(SevenElementsCriteria.PERFORM_ATTUNEMENT);
		register(SevenElementsCriteria.REACTION_TRIGGERED);
	}

	private static void register(Criterion<?> criterion) {
		Criteria.register(criterion);
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

	static <A, T> Optional<A> optionalStrictParse(Codec<A> codec, DynamicOps<T> ops, @Nullable T input) {
		return input == null
			? Optional.empty()
			: codec.parse(ops, input).resultOrPartial(err -> { throw new JsonParseException(err); });
	}

	static <A, T> Optional<T> optionalEncodeStart(Codec<A> codec, DynamicOps<T> ops, Optional<A> input) {
		return input
			.flatMap(v -> codec.encodeStart(ops, v).result());
	}
}
