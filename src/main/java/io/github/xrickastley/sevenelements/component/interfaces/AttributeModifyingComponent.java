package io.github.xrickastley.sevenelements.component.interfaces;

import com.google.common.collect.HashMultimap;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

public interface AttributeModifyingComponent {
	public HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(ItemStack stack, AttributeModifierSlot slot);

	public static void applyModifiers(LivingEntity entity, AttributeModifierSlot slot, ItemStack stack) {
		AttributeModifyingComponent
			.getModifiers(slot, stack)
			.forEach(attributes -> entity.getAttributes().addTemporaryModifiers(attributes));
	}

	public static void applyModifiers(ItemStack stack, AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer) {
		AttributeModifyingComponent
			.getModifiers(slot, stack)
			.forEach(attributes -> attributes.forEach(attributeModifierConsumer));
	}

	public static void removeModifiers(LivingEntity entity, AttributeModifierSlot slot, ItemStack stack) {
		AttributeModifyingComponent
			.getModifiers(slot, stack)
			.forEach(attributes -> entity.getAttributes().removeModifiers(attributes));
	}

	private static Stream<HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier>> getModifiers(AttributeModifierSlot slot, ItemStack stack) {
		return stack
			.getComponents()
			.stream()
			.<AttributeModifyingComponent>mapMulti((component, mapper) -> {
				if (component.value() instanceof final AttributeModifyingComponent amc) mapper.accept(amc);
			})
			.map(amc -> amc.getModifiers(stack, slot));
	}
}
