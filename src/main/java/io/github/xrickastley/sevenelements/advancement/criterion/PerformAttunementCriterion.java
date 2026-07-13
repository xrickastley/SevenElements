package io.github.xrickastley.sevenelements.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class PerformAttunementCriterion extends SimpleCriterionTrigger<PerformAttunementCriterion.Conditions> {
	public Codec<Conditions> codec() {
		return Conditions.CODEC;
	}

	public void trigger(ServerPlayer player, Element attunement) {
		this.trigger(player, Functions.withArgument(Conditions::requirementsMet, attunement));
	}

	public record Conditions(Optional<ContextAwarePredicate> player, Optional<Element> element) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<PerformAttunementCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
			Element.CODEC.optionalFieldOf("element").forGetter(Conditions::element)
		).apply(instance, Conditions::new));

		public boolean requirementsMet(Element attunement) {
			return SevenElementsCriteria.emptyOrEqual(element, attunement);
		}
	}
}
