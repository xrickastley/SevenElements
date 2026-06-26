package io.github.xrickastley.sevenelements.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistryKeys;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.network.ServerPlayerEntity;

public class ReactionTriggeredCriterion extends AbstractCriterion<ReactionTriggeredCriterion.Conditions> {
	public Codec<Conditions> getConditionsCodec() {
		return Conditions.CODEC;
	}

	public void trigger(ServerPlayerEntity player, ElementalReaction reaction, Element triggeringElement) {
		this.trigger(player, Functions.withArgument(Conditions::requirementsMet, reaction, triggeringElement));
	}

	public record Conditions(Optional<LootContextPredicate> player, Optional<RegistryEntryList<ElementalReaction>> elementalReactions, Optional<Element> triggeringElement) implements AbstractCriterion.Conditions {
		public static final Codec<ReactionTriggeredCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
			RegistryCodecs.entryList(SevenElementsRegistryKeys.ELEMENTAL_REACTION).optionalFieldOf("elemental_reactions").forGetter(Conditions::elementalReactions),
			Element.CODEC.optionalFieldOf("triggering_element").forGetter(Conditions::triggeringElement)
		).apply(instance, Conditions::new));

		public boolean requirementsMet(ElementalReaction reaction, Element element) {
			return SevenElementsCriteria.emptyOrEqual(triggeringElement, element)
				&& SevenElementsCriteria.emptyOrPasses(elementalReactions, reaction::isIn);
		}
	}
}
