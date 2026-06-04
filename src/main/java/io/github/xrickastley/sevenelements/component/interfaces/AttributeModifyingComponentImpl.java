package io.github.xrickastley.sevenelements.component.interfaces;

import com.google.common.collect.HashMultimap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;

public class AttributeModifyingComponentImpl {
	private static final List<ComponentKey<? extends AttributeModifyingComponent>> ATTRIBUTE_COMPONENTS = new ArrayList<>();

	public static void addComponent(ComponentKey<? extends AttributeModifyingComponent> key) {
		AttributeModifyingComponentImpl.ATTRIBUTE_COMPONENTS.add(key);
	}

	static Stream<HashMultimap<EntityAttribute, EntityAttributeModifier>> getModifiers(EquipmentSlot slot, ItemStack stack) {
		return ATTRIBUTE_COMPONENTS
			.stream()
			.<AttributeModifyingComponent>mapMulti((key, consumer) -> ClassInstanceUtil.ifPresentMapped(stack, getComponent(key), consumer))
			.map(amc -> amc.getModifiers(stack, slot));
	}

	private static Function<ItemStack, @Nullable AttributeModifyingComponent> getComponent(ComponentKey<? extends AttributeModifyingComponent> componentKey) {
		return stack -> {
			try {
				return componentKey.get(stack);
			} catch (Exception e) {
				return null;
			}
		};
	}
}
