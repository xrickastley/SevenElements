package io.github.xrickastley.sevenelements.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistryKeys;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.server.level.ServerPlayer;

public class ReactionTriggeredCriterion extends SimpleCriterionTrigger<ReactionTriggeredCriterion.Conditions> {
	public Codec<Conditions> codec() {
		return Conditions.CODEC;
	}

	public void trigger(ServerPlayer player, ElementalReaction reaction, Element triggeringElement) {
		this.trigger(player, Functions.withArgument(Conditions::requirementsMet, reaction, triggeringElement));
	}

	public record Conditions(Optional<ContextAwarePredicate> player, Optional<HolderSet<ElementalReaction>> elementalReactions, Optional<Element> triggeringElement) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<ReactionTriggeredCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
			RegistryCodecs.homogeneousList(SevenElementsRegistryKeys.ELEMENTAL_REACTION).optionalFieldOf("elemental_reactions").forGetter(Conditions::elementalReactions),
			Element.CODEC.optionalFieldOf("triggering_element").forGetter(Conditions::triggeringElement)
		).apply(instance, Conditions::new));

		public boolean requirementsMet(ElementalReaction reaction, Element element) {
			return SevenElementsCriteria.emptyOrEqual(triggeringElement, element)
				&& SevenElementsCriteria.emptyOrPasses(elementalReactions, reaction::isIn);
		}
	}
}
