package io.github.xrickastley.sevenelements.component.interfaces;

import com.google.common.collect.HashMultimap;

import java.util.stream.Stream;

import org.apache.commons.lang3.function.TriConsumer;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers.Display;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface AttributeModifyingComponent {
	public HashMultimap<Holder<Attribute>, AttributeModifier> getModifiers(ItemStack stack, EquipmentSlotGroup slot);

	public static void applyModifiers(LivingEntity entity, EquipmentSlotGroup slot, ItemStack stack) {
		AttributeModifyingComponent
			.getModifiers(slot, stack)
			.forEach(attributes -> entity.getAttributes().addTransientAttributeModifiers(attributes));
	}

	public static void applyModifiers(ItemStack stack, EquipmentSlotGroup slot, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> attributeModifierConsumer) {
		AttributeModifyingComponent
			.getModifiers(slot, stack)
			.forEach(attributes -> attributes.forEach((attribute, modifier) -> attributeModifierConsumer.accept(attribute, modifier, Display.attributeModifiers())));
	}

	public static void removeModifiers(LivingEntity entity, EquipmentSlotGroup slot, ItemStack stack) {
		AttributeModifyingComponent
			.getModifiers(slot, stack)
			.forEach(attributes -> entity.getAttributes().removeAttributeModifiers(attributes));
	}

	private static Stream<HashMultimap<Holder<Attribute>, AttributeModifier>> getModifiers(EquipmentSlotGroup slot, ItemStack stack) {
		return stack
			.getComponents()
			.stream()
			.<AttributeModifyingComponent>mapMulti((component, mapper) -> {
				if (component.value() instanceof final AttributeModifyingComponent amc) mapper.accept(amc);
			})
			.map(amc -> amc.getModifiers(stack, slot));
	}
}
