package io.github.xrickastley.sevenelements.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ElementalInfusionCriterion extends SimpleCriterionTrigger<ElementalInfusionCriterion.Conditions> {
	public Codec<io.github.xrickastley.sevenelements.advancement.criterion.ElementalInfusionCriterion.Conditions> codec() {
		return io.github.xrickastley.sevenelements.advancement.criterion.ElementalInfusionCriterion.Conditions.CODEC;
	}

	public void trigger(ServerPlayer player, ItemStack stack, Element infused) {
		this.trigger(player, Functions.withArgument(io.github.xrickastley.sevenelements.advancement.criterion.ElementalInfusionCriterion.Conditions::requirementsMet, stack, infused));
	}

	public record Conditions(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, Optional<Element> element) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<ElementalInfusionCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(io.github.xrickastley.sevenelements.advancement.criterion.ElementalInfusionCriterion.Conditions::player),
			ItemPredicate.CODEC.optionalFieldOf("item").forGetter(io.github.xrickastley.sevenelements.advancement.criterion.ElementalInfusionCriterion.Conditions::item),
			Element.CODEC.optionalFieldOf("element").forGetter(io.github.xrickastley.sevenelements.advancement.criterion.ElementalInfusionCriterion.Conditions::element)
		).apply(instance, io.github.xrickastley.sevenelements.advancement.criterion.ElementalInfusionCriterion.Conditions::new));

		public boolean requirementsMet(ItemStack stack, Element infused) {
			return (element.isEmpty() || element.get() == infused)
				&& (item.isEmpty() || item.get().test(stack));
		}
	}
}
