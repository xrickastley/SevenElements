package io.github.xrickastley.sevenelements.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class ElementalInfusionCriterion extends AbstractCriterion<ElementalInfusionCriterion.Conditions> {
	public Codec<Conditions> getConditionsCodec() {
		return Conditions.CODEC;
	}

	public void trigger(ServerPlayerEntity player, ItemStack stack, Element infused) {
		this.trigger(player, Functions.withArgument(Conditions::requirementsMet, stack, infused));
	}

	public record Conditions(Optional<LootContextPredicate> player, Optional<ItemPredicate> item, Optional<Element> element) implements AbstractCriterion.Conditions {
		public static final Codec<ElementalInfusionCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
			ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Conditions::item),
			Element.CODEC.optionalFieldOf("element").forGetter(Conditions::element)
		).apply(instance, Conditions::new));

		public boolean requirementsMet(ItemStack stack, Element infused) {
			return (element.isEmpty() || element.get() == infused)
				&& (item.isEmpty() || item.get().test(stack));
		}
	}
}
