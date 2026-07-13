package io.github.xrickastley.sevenelements.component.interfaces;

import com.google.common.collect.HashMultimap;

import java.util.stream.Stream;

import org.apache.commons.lang3.function.TriConsumer;

import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers.Display;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * An interface for data components that modify attributes on their own. <br> <br>
 *
 * This allows other data components than the {@link ItemAttributeModifiers} to easily apply
 * attribute modifiers to entities holding an item stack with their data component. <br> <br>
 *
 * Note that modifiers may <b>not</b>, under any circumstances, be <i>too</i> dynamic or
 * conditional, i.e. give the entity an attribute modifier <b>if</b> they're flying; attribute
 * modifiers are calculated <b>only</b> when the stack in the attribute modifier slot changes. To
 * avoid unintentional errors when making conditional modifiers, the returned modifier <b>must
 * always</b> be the same for the stack until it changes.
 */
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
			.<AttributeModifyingComponent>mapMulti((component, mapper) ->
				ClassInstanceUtil.ifInstanceOf(component.value(), AttributeModifyingComponent.class, mapper::accept)
			)
			.map(amc -> amc.getModifiers(stack, slot));
	}
}
