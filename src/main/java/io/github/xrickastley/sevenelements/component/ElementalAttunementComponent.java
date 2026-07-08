package io.github.xrickastley.sevenelements.component;

import com.google.common.collect.HashMultimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.interfaces.AttributeModifyingComponent;
import io.github.xrickastley.sevenelements.component.interfaces.ElementModifyingComponent;
import io.github.xrickastley.sevenelements.component.interfaces.TooltipProvider;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes.ModifierType;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
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
	implements AttributeModifyingComponent, ElementModifyingComponent, TooltipProvider
{
	private static final Logger LOGGER = SevenElements.sublogger();

	public static final ComponentKey<ElementalAttunementComponent> KEY = ComponentRegistry.getOrCreate(SevenElements.identifier("elemental_attunement"), ElementalAttunementComponent.class);

	public static final Codec<Element> CODEC = Element.CODEC.comapFlatMap(ElementalAttunementComponent::validate, e -> e);

	private static DataResult<Element> validate(Element element) {
		if (ElementalAttunementComponent.isValidForAttunement(element))
			return DataResult.success(element);
		else
			return DataResult.error(() -> "Not a valid Element for attunement: " + element);
	}

	public ElementalAttunementComponent(ItemStack stack) {
		super(stack);
	}

	public static void applyAttunement(ItemStack stack, Element element) {
		final ElementalAttunementComponent component = ElementalAttunementComponent.get(stack);

		if (component == null) return;

		component.setElementalAttunement(element);

		if (ClassInstanceUtil.mapOrNull(ElementalInfusionComponent.get(stack), ElementalInfusionComponent::getElement) != element)
			ElementalInfusionComponent.removeInfusion(stack);
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
			.ofNullable(ElementalAttunementComponent.get(stack))
			.map(ElementalAttunementComponent::hasElementalAttunement)
			.orElse(false);
	}

	public static boolean isValidForAttunement(Element element) {
		return element != Element.PHYSICAL
			&& SevenElementsAttributes.hasElementalAttribute(element);
	}

	public static @Nullable ElementalAttunementComponent get(ItemStack stack) {
		return ElementalAttunementComponent.KEY.maybeGet(stack).orElse(null);
	}

	public @Nullable Element element() {
		return this.hasElementalAttunement()
			? ElementalAttunementComponent.CODEC
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
			((NbtString) ElementalAttunementComponent.CODEC.encodeStart(NbtOps.INSTANCE, element)
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
	public Text getSymbol() {
		return Text.translatable("symbols.seven-elements.elemental_infusion.elemental_attunment");
	}

	@Override
	public boolean shouldModify(ElementalInfusionComponent infusion) {
		return infusion.getElement() == this.element();
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
		ElementModifyingComponent.addElementModifyingComponent(ElementalAttunementComponent.KEY);
	}
}
