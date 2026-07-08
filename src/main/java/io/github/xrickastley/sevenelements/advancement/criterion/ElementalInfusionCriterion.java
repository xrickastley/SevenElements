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
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ElementalInfusionCriterion extends AbstractCriterion<ElementalInfusionCriterion.Conditions> {
	public static final Identifier ID = SevenElements.identifier("elemental_infusion");

	@Override
	protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
		final Optional<ItemPredicate> item = ItemPredicate.fromJson(obj.get("item"));
		final Optional<Element> element = SevenElementsCriteria.optionalStrictParse(Element.CODEC, JsonOps.INSTANCE, obj.get("element"));

		return new Conditions(playerPredicate, item, element);
	}

	public void trigger(ServerPlayerEntity player, ItemStack stack, Element infused) {
		this.trigger(player, Functions.withArgument(ElementalInfusionCriterion.Conditions::requirementsMet, stack, infused));
	}

	public class Conditions extends AbstractCriterionConditions {
		private final Optional<ItemPredicate> item;
		private final Optional<Element> element;

		private Conditions(Optional<LootContextPredicate> player, Optional<ItemPredicate> item, Optional<Element> element) {
			super(player);

			this.item = item;
			this.element = element;
		}

		public Optional<ItemPredicate> item() {
			return item;
		}

		public Optional<Element> element() {
			return element;
		}

		public boolean requirementsMet(ItemStack stack, Element infused) {
			return SevenElementsCriteria.emptyOrEqual(element, infused)
				&& SevenElementsCriteria.emptyOrPasses(item, ItemPredicate::test, stack);
		}

		@Override
		public JsonObject toJson() {
			JsonObject jsonObject = super.toJson();
			jsonObject.add("item", this.item.map(ItemPredicate::toJson).orElse(JsonNull.INSTANCE));
			jsonObject.add("element", SevenElementsCriteria.optionalEncodeStart(Element.CODEC, JsonOps.INSTANCE, this.element).orElse(JsonNull.INSTANCE));
			return jsonObject;
		}
	}
}
