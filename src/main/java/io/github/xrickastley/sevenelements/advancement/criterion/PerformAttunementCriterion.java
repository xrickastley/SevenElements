package io.github.xrickastley.sevenelements.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class PerformAttunementCriterion extends AbstractCriterion<PerformAttunementCriterion.Conditions> {
	public Codec<Conditions> getConditionsCodec() {
		return Conditions.CODEC;
	}

	public void trigger(ServerPlayerEntity player, Element attunement) {
		this.trigger(player, Functions.withArgument(Conditions::requirementsMet, attunement));
	}

	public record Conditions(Optional<LootContextPredicate> player, Optional<Element> element) implements AbstractCriterion.Conditions {
		public static final Codec<PerformAttunementCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
			Element.CODEC.optionalFieldOf("element").forGetter(Conditions::element)
		).apply(instance, Conditions::new));

		public boolean requirementsMet(Element attunement) {
			return SevenElementsCriteria.emptyOrEqual(element, attunement);
		}
	}
}
