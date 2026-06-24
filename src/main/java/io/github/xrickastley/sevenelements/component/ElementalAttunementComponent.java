package io.github.xrickastley.sevenelements.component;

import com.google.common.collect.HashMultimap;
import com.mojang.serialization.Codec;

import java.util.function.Consumer;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.interfaces.AttributeModifyingComponent;
import io.github.xrickastley.sevenelements.component.interfaces.ElementModifyingComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes.ModifierType;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public record ElementalAttunementComponent(Element element) implements AttributeModifyingComponent, ElementModifyingComponent, TooltipAppender {
	public static final Codec<ElementalAttunementComponent> CODEC = Element.CODEC.xmap(ElementalAttunementComponent::new, ElementalAttunementComponent::element);

	public static void applyAttunement(ItemStack stack, Element element) {
		stack.set(
			SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT,
			new ElementalAttunementComponent(element)
		);
	}

	public static boolean removeAttunement(ItemStack stack) {
		if (!ElementalAttunementComponent.hasAttunement(stack)) return false;

		stack.remove(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT);

		return true;
	}

	public static boolean hasAttunement(ItemStack stack) {
		return stack.contains(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT);
	}

	@Override
	public HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(ItemStack itemStack, AttributeModifierSlot slot) {
		final HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeMultimap = HashMultimap.create();

		if (itemStack.getItem() instanceof final ArmorItem armor) {
			if (slot == AttributeModifierSlot.forEquipmentSlot(armor.getSlotType()))
				attributeMultimap.put(SevenElementsAttributes.getElementalAttribute(element, ModifierType.RES), this.createAttributeModifier(slot, 15, Operation.ADD_VALUE));
		} else if (slot == AttributeModifierSlot.MAINHAND) {
			attributeMultimap.put(SevenElementsAttributes.getElementalAttribute(element, ModifierType.DMG_BONUS), this.createAttributeModifier(slot, 50, Operation.ADD_VALUE));
		}

		return attributeMultimap;
	}

	public Identifier getModifierId(StringIdentifiable suffix) {
		return SevenElements.identifier(element.toString().toLowerCase() + "/" + suffix.asString().toLowerCase());
	}

	public EntityAttributeModifier createAttributeModifier(StringIdentifiable suffix, double value, EntityAttributeModifier.Operation operation) {
		return new EntityAttributeModifier(this.getModifierId(suffix), value, operation);
	}

	@Override
	public Text getSymbol() {
		return Text.translatable("symbols.seven-elements.elemental_infusion.elemental_attunment");
	}

	@Override
	public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
		tooltip.accept(
			Text.empty()
				.append(Text.translatable("item.seven-elements.components.attunement.attunement")).formatted(Formatting.GRAY)
				.append(
					TextHelper.color(
						Text.literal("[")
							.append(element.getText())
							.append("]"),
						this.element.getDamageColor()
					)
				)
		);
	}
}
