package io.github.xrickastley.sevenelements.advancement.criterion;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import java.util.Optional;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PerformAttunementCriterion extends AbstractCriterion<PerformAttunementCriterion.Conditions> {
	public static final Identifier ID = SevenElements.identifier("perform_attunement");

	@Override
	protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
		final Optional<Element> element = SevenElementsCriteria.optionalStrictParse(Element.CODEC, JsonOps.INSTANCE, obj.get("element"));

		return new Conditions(playerPredicate, element);
	}

	public void trigger(ServerPlayerEntity player, Element attunement) {
		this.trigger(player, Functions.withArgument(Conditions::requirementsMet, attunement));
	}

	public class Conditions extends AbstractCriterionConditions {
		private final Optional<Element> element;

		private Conditions(Optional<LootContextPredicate> player, Optional<Element> element) {
			super(player);

			this.element = element;
		}

		public boolean requirementsMet(Element attunement) {
			return SevenElementsCriteria.emptyOrEqual(element, attunement);
		}

		@Override
		public JsonObject toJson() {
			JsonObject jsonObject = super.toJson();
			jsonObject.add("element", SevenElementsCriteria.optionalEncodeStart(Element.CODEC, JsonOps.INSTANCE, this.element).orElse(JsonNull.INSTANCE));
			return jsonObject;
		}
	}
}
