package io.github.xrickastley.sevenelements.component;

import com.google.common.collect.HashMultimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.function.Consumer;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.interfaces.AttributeModifyingComponent;
import io.github.xrickastley.sevenelements.component.interfaces.ElementModifyingComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes.ModifierType;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record ElementalAttunementComponent(Element element) implements AttributeModifyingComponent, ElementModifyingComponent, TooltipProvider {
	public static final Codec<ElementalAttunementComponent> CODEC = Element.CODEC.comapFlatMap(ElementalAttunementComponent::validate, ElementalAttunementComponent::element);

	private static DataResult<ElementalAttunementComponent> validate(Element element) {
		if (ElementalAttunementComponent.isValidForAttunement(element))
			return DataResult.success(new ElementalAttunementComponent(element));
		else
			return DataResult.error(() -> "Not a valid Element for attunement: " + element);
	}

	public static void applyAttunement(ItemStack stack, Element element) {
		stack.set(
			SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT,
			new ElementalAttunementComponent(element)
		);

		if (ClassInstanceUtil.mapOrNull(stack.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT), ElementalInfusionComponent::getElement) != element)
			stack.remove(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);
	}

	public static boolean removeAttunement(ItemStack stack) {
		if (!ElementalAttunementComponent.hasAttunement(stack)) return false;

		stack.remove(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT);

		return true;
	}

	public static boolean hasAttunement(ItemStack stack) {
		return stack.has(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT);
	}

	public static boolean isValidForAttunement(Element element) {
		return element != Element.PHYSICAL
			&& SevenElementsAttributes.hasElementalAttribute(element);
	}

	@Override
	public HashMultimap<Holder<Attribute>, AttributeModifier> getModifiers(ItemStack itemStack, EquipmentSlotGroup slot) {
		final HashMultimap<Holder<Attribute>, AttributeModifier> attributeMultimap = HashMultimap.create();

		if (itemStack.has(DataComponents.EQUIPPABLE)) {
			if (slot == EquipmentSlotGroup.bySlot(itemStack.get(DataComponents.EQUIPPABLE).slot()))
				attributeMultimap.put(SevenElementsAttributes.getElementalAttribute(element, ModifierType.RES), this.createAttributeModifier(slot, 15, Operation.ADD_VALUE));
		} else if (slot == EquipmentSlotGroup.MAINHAND) {
			attributeMultimap.put(SevenElementsAttributes.getElementalAttribute(element, ModifierType.DMG_BONUS), this.createAttributeModifier(slot, 50, Operation.ADD_VALUE));
		}

		return attributeMultimap;
	}

	public Identifier getModifierId(StringRepresentable suffix) {
		return SevenElements.identifier(element.toString().toLowerCase() + "/" + suffix.getSerializedName().toLowerCase());
	}

	public AttributeModifier createAttributeModifier(StringRepresentable suffix, double value, AttributeModifier.Operation operation) {
		return new AttributeModifier(this.getModifierId(suffix), value, operation);
	}

	@Override
	public Component getSymbol() {
		return Component.translatable("symbols.seven-elements.elemental_infusion.elemental_attunment");
	}

	@Override
	public boolean shouldModify(ElementalInfusionComponent infusion) {
		return infusion.getElement() == this.element;
	}

	@Override
	public void addToTooltip(Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
		textConsumer.accept(
			Component.empty()
				.append(Component.translatable("item.seven-elements.components.attunement.attunement")).withStyle(ChatFormatting.GRAY)
				.append(
					TextHelper.color(
						Component.literal("[")
							.append(element.getText())
							.append("]"),
						this.element.getDamageColor()
					)
				)
		);
	}
}
