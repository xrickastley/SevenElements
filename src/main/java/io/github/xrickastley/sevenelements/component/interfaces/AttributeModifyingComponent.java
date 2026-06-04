package io.github.xrickastley.sevenelements.component.interfaces;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.function.BiConsumer;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;

public interface AttributeModifyingComponent {
	public HashMultimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, EquipmentSlot slot);

	public static void addAttributeComponent(ComponentKey<? extends AttributeModifyingComponent> key) {
		AttributeModifyingComponentImpl.addComponent(key);
	}

	public static void applyModifiers(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
		AttributeModifyingComponentImpl
			.getModifiers(slot, stack)
			.forEach(attributes -> entity.getAttributes().addTemporaryModifiers(attributes));
	}

	public static void applyModifiers(ItemStack stack, EquipmentSlot slot, BiConsumer<EntityAttribute, EntityAttributeModifier> attributeModifierConsumer) {
		AttributeModifyingComponentImpl
			.getModifiers(slot, stack)
			.forEach(attributes -> attributes.forEach(attributeModifierConsumer));
	}

	public static void removeModifiers(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
		AttributeModifyingComponentImpl
			.getModifiers(slot, stack)
			.forEach(attributes -> entity.getAttributes().removeModifiers(attributes));
	}

	public static Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(EquipmentSlot slot, ItemStack stack) {
		return AttributeModifyingComponentImpl
			.getModifiers(slot, stack)
			.collect(LinkedHashMultimap::create, Multimap::putAll, Multimap::putAll);
	}
}
