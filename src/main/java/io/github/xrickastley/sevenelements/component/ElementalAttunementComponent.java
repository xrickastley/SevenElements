package io.github.xrickastley.sevenelements.component;

import com.google.common.collect.HashMultimap;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.interfaces.AttributeModifyingComponent;
import io.github.xrickastley.sevenelements.component.interfaces.TooltipProvider;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes.ModifierType;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;

public final class ElementalAttunementComponent
	extends ItemComponent
	implements AttributeModifyingComponent, TooltipProvider
{
	private static final Logger LOGGER = SevenElements.sublogger();

	public static final ComponentKey<ElementalAttunementComponent> KEY = ComponentRegistry.getOrCreate(SevenElements.identifier("elemental_attunement"), ElementalAttunementComponent.class);

	public ElementalAttunementComponent(ItemStack stack) {
		super(stack);
	}

	public static void applyAttunement(ItemStack stack, Element element) {
		final ElementalAttunementComponent component = ElementalAttunementComponent.get(stack);

		if (component == null) return;

		component.setElementalAttunement(element);

		return;
	}

	public static boolean removeAttunement(ItemStack stack) {
		if (!ElementalAttunementComponent.hasAttunement(stack)) return false;

		final ElementalAttunementComponent component = ElementalAttunementComponent.get(stack);

		if (component == null) return false;

		component.remove("element");

		return true;
	}

	public static boolean hasAttunement(ItemStack stack) {
		return Optional
			.of(ElementalAttunementComponent.get(stack))
			.map(ElementalAttunementComponent::hasElementalAttunement)
			.orElse(false);
	}

	public static @Nullable ElementalAttunementComponent get(ItemStack stack) {
		return ElementalAttunementComponent.KEY.maybeGet(stack).orElse(null);
	}

	public @Nullable Element element() {
		return this.hasElementalAttunement()
			? Element.CODEC
				.parse(NbtOps.INSTANCE, this.getTag("element", NbtElement.STRING_TYPE))
				.resultOrPartial(LOGGER::error)
				.orElseThrow()
			: null;
	}

	public boolean hasElementalAttunement() {
		return this.hasTag("element", NbtElement.STRING_TYPE);
	}

	private void setElementalAttunement(Element element) {
		this.putString(
			"element",
			((NbtString) Element.CODEC.encodeStart(NbtOps.INSTANCE, element)
				.resultOrPartial(LOGGER::error)
				.orElseThrow()).asString()
		);
	}

	@Override
	public HashMultimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack itemStack, EquipmentSlot slot) {
		final HashMultimap<EntityAttribute, EntityAttributeModifier> attributeMultimap = HashMultimap.create();
		
		if (!this.hasElementalAttunement()) return attributeMultimap;

		if (itemStack.getItem() instanceof final ArmorItem armor) {
			if (slot == armor.getSlotType())
				attributeMultimap.put(SevenElementsAttributes.getElementalAttribute(this.element(), ModifierType.RES), this.createAttributeModifier(slot, 15, Operation.ADDITION));
		} else if (slot == EquipmentSlot.MAINHAND) {
			attributeMultimap.put(SevenElementsAttributes.getElementalAttribute(this.element(), ModifierType.DMG_BONUS), this.createAttributeModifier(slot, 50, Operation.ADDITION));
		}

		return attributeMultimap;
	}

	public Identifier getModifierId(EquipmentSlot slot) {
		return SevenElements.identifier(this.element().toString().toLowerCase() + "/" + slot.getName().toLowerCase());
	}

	public EntityAttributeModifier createAttributeModifier(EquipmentSlot slot, double value, EntityAttributeModifier.Operation operation) {
		final String id = this.getModifierId(slot).toString();

		return new EntityAttributeModifier(
			UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8)),
			id,
			value,
			operation
		);
	}

	@Override
	public void appendTooltip(@Nullable PlayerEntity player, TooltipContext context, Consumer<Text> textConsumer) {
		if (!this.hasElementalAttunement()) return;

		textConsumer.accept(
			Text.empty()
				.append(Text.translatable("item.seven-elements.components.attunement.attunement")).formatted(Formatting.GRAY)
				.append(
					TextHelper.color(
						Text.literal("[")
							.append(this.element().getText())
							.append("]"),
						this.element().getDamageColor()
					)
				)
		);
	}

	static {
		AttributeModifyingComponent.addAttributeComponent(ElementalAttunementComponent.KEY);
	}
}
