package io.github.xrickastley.sevenelements.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ElementalInfusionCriterion extends SimpleCriterionTrigger<ElementalInfusionCriterion.Conditions> {
	public Codec<ElementalInfusionCriterion.Conditions> codec() {
		return ElementalInfusionCriterion.Conditions.CODEC;
	}

	public void trigger(ServerPlayer player, ItemStack stack, Element infused) {
		this.trigger(player, Functions.withArgument(Conditions::requirementsMet, stack, infused));
	}

	public record Conditions(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, Optional<Element> element) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<ElementalInfusionCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
			ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Conditions::item),
			Element.CODEC.optionalFieldOf("element").forGetter(Conditions::element)
		).apply(instance, Conditions::new));

		public boolean requirementsMet(ItemStack stack, Element infused) {
			return SevenElementsCriteria.emptyOrEqual(element, infused)
				&& SevenElementsCriteria.emptyOrPasses(item, ItemPredicate::test, stack);
		}
	}
}
