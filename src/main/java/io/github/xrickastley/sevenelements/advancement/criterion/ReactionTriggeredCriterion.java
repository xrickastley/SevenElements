package io.github.xrickastley.sevenelements.advancement.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.Optional;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistryKeys;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.dynamic.SevenElementsCodecs;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ReactionTriggeredCriterion extends AbstractCriterion<ReactionTriggeredCriterion.Conditions> {
	public static final Identifier ID = SevenElements.identifier("reaction_triggered");

	private static final Codec<RegistryEntryList<ElementalReaction>> REACTION_CODEC
		= RegistryCodecs.entryList(SevenElementsRegistryKeys.ELEMENTAL_REACTION);

	@Override
	protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
		final RegistryOps<JsonElement> registryOps = RegistryOps.of(JsonOps.INSTANCE, SevenElementsCodecs.STATIC_REGISTRY_LOOKUP);

		final Optional<RegistryEntryList<ElementalReaction>> elementalReactions = SevenElementsCriteria.optionalStrictParse(REACTION_CODEC, registryOps, obj.get("elemental_reactions"));
		final Optional<Element> element = SevenElementsCriteria.optionalStrictParse(Element.CODEC, registryOps, obj.get("triggering_element"));

		return new Conditions(playerPredicate, elementalReactions, element);
	}

	public void trigger(ServerPlayerEntity player, ElementalReaction reaction, Element triggeringElement) {
		this.trigger(player, Functions.withArgument(Conditions::requirementsMet, reaction, triggeringElement));
	}

	public class Conditions extends AbstractCriterionConditions {
		private final Optional<RegistryEntryList<ElementalReaction>> elementalReactions;
		private final Optional<Element> triggeringElement;

		private Conditions(Optional<LootContextPredicate> player, Optional<RegistryEntryList<ElementalReaction>> elementalReactions, Optional<Element> triggeringElement) {
			super(player);

			this.elementalReactions = elementalReactions;
			this.triggeringElement = triggeringElement;
		}

		public boolean requirementsMet(ElementalReaction reaction, Element element) {
			return SevenElementsCriteria.emptyOrEqual(triggeringElement, element)
				&& SevenElementsCriteria.emptyOrPasses(elementalReactions, reaction::isIn);
		}

		@Override
		public JsonObject toJson() {
			final RegistryOps<JsonElement> registryOps = RegistryOps.of(JsonOps.INSTANCE, SevenElementsCodecs.STATIC_REGISTRY_LOOKUP);

			JsonObject jsonObject = super.toJson();
			jsonObject.add("elemental_reactions", SevenElementsCriteria.optionalEncodeStart(REACTION_CODEC, registryOps, this.elementalReactions).orElse(JsonNull.INSTANCE));
			jsonObject.add("triggering_element", SevenElementsCriteria.optionalEncodeStart(Element.CODEC, registryOps, this.triggeringElement).orElse(JsonNull.INSTANCE));
			return jsonObject;
		}
	}
}
