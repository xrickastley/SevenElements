package io.github.xrickastley.sevenelements.advancement.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Optional;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
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
	public Identifier getId() {
		return ID;
	}

	@Override
	protected Conditions conditionsFromJson(JsonObject obj, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
		final Optional<ItemPredicate> item = Optional.ofNullable(
			ClassInstanceUtil.mapOrNull(obj.get("item"), ItemPredicate::fromJson)
		);

		final Optional<Element> element = Optional.ofNullable(
			ClassInstanceUtil.mapOrNull(obj.get("element"), Functions.compose(JsonElement::getAsString, s -> Element.valueOf(s)))
		);

		return new Conditions(playerPredicate, item, element);
	}

	public void trigger(ServerPlayerEntity player, ItemStack stack, Element infused) {
		this.trigger(player, Functions.withArgument(ElementalInfusionCriterion.Conditions::requirementsMet, stack, infused));
	}

	public class Conditions extends AbstractCriterionConditions {
		private final Optional<ItemPredicate> item;
		private final Optional<Element> element;

		private Conditions(LootContextPredicate player, Optional<ItemPredicate> item, Optional<Element> element) {
			super(ElementalInfusionCriterion.ID, player);

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
			return (element.isEmpty() || element.get() == infused)
				&& (item.isEmpty() || item.get().test(stack));
		}
	}
}
