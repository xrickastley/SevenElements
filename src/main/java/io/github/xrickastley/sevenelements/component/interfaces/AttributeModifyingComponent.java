package io.github.xrickastley.sevenelements.component.interfaces;

import com.google.common.collect.HashMultimap;

import java.util.stream.Stream;

import org.apache.commons.lang3.function.TriConsumer;

import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent.Display;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * An interface for data components that modify attributes on their own. <br> <br>
 *
 * This allows other data components than the {@link AttributeModifiersComponent} to easily apply
 * attribute modifiers to entities holding an item stack with their data component. <br> <br>
 *
 * Note that modifiers may <b>not</b>, under any circumstances, be <i>too</i> dynamic or
 * conditional, i.e. give the entity an attribute modifier <b>if</b> they're flying; attribute
 * modifiers are calculated <b>only</b> when the stack in the attribute modifier slot changes. To
 * avoid unintentional errors when making conditional modifiers, the returned modifier <b>must
 * always</b> be the same for the stack until it changes.
 */
public interface AttributeModifyingComponent {
	public HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(ItemStack stack, AttributeModifierSlot slot);

	public static void applyModifiers(LivingEntity entity, AttributeModifierSlot slot, ItemStack stack) {
		AttributeModifyingComponent
			.getModifiers(slot, stack)
			.forEach(attributes -> entity.getAttributes().addTemporaryModifiers(attributes));
	}

	public static void applyModifiers(ItemStack stack, AttributeModifierSlot slot, TriConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier, AttributeModifiersComponent.Display> attributeModifierConsumer) {
		AttributeModifyingComponent
			.getModifiers(slot, stack)
			.forEach(attributes -> attributes.forEach((attribute, modifier) -> attributeModifierConsumer.accept(attribute, modifier, Display.getDefault())));
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
			.<AttributeModifyingComponent>mapMulti((component, mapper) ->
				ClassInstanceUtil.ifInstanceOf(component.value(), AttributeModifyingComponent.class, mapper::accept)
			)
			.map(amc -> amc.getModifiers(stack, slot));
	}
}
