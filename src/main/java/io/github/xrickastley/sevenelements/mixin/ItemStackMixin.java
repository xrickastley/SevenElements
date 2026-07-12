package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.component.ElementalAttunementComponent;
import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.component.interfaces.AttributeModifyingComponent;
import io.github.xrickastley.sevenelements.component.interfaces.ElementModifyingComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.interfaces.IItemStack;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;
import io.github.xrickastley.sevenelements.util.TextHelper;
import io.github.xrickastley.sevenelements.util.Util;

import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder, IItemStack {
	@Shadow
	public abstract <T extends TooltipAppender> void appendComponentTooltip(ComponentType<T> componentType, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type);

	@Shadow
	public abstract Item getItem();

	@Shadow
	public abstract Text getName();

	@ModifyReturnValue(
		method = "getName",
		at = @At("RETURN")
	)
	private Text modifyName(Text original) {
		final @Nullable ElementalInfusionComponent component = this.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);

		if (
			component == null
			|| !component.hasElementalInfusion()
			|| Util.isCalledBy(ItemStack.class, "sevenelements$getTrueName", 1)
		) return original;

		final Element element = component.getElement();

		final String symbols = this.getComponents()
			.stream()
			.<ElementModifyingComponent>mapMulti((component2, mapper) ->
				ClassInstanceUtil.ifInstanceOf(component2.value(), ElementModifyingComponent.class, mapper::accept)
			)
			.filter(Functions.withArgument(ElementModifyingComponent::shouldModify, component))
			.map(emc -> " " + emc.getSymbol().getString())
			.collect(Collectors.joining());

		return Text.empty()
			.append(original)
			.append(TextHelper.noModifiers(TextHelper.color(" [" + element.getString() + symbols + "]", element.getDamageColor())));
	}

	@Inject(
		method = "appendTooltip",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/ItemStack;appendComponentTooltip(Lnet/minecraft/component/ComponentType;Lnet/minecraft/item/Item$TooltipContext;Lnet/minecraft/component/type/TooltipDisplayComponent;Ljava/util/function/Consumer;Lnet/minecraft/item/tooltip/TooltipType;)V",
			ordinal = 15,
			shift = At.Shift.AFTER
		)
	)
	private void addAttunementData(Item.TooltipContext context, TooltipDisplayComponent displayComponent, @Nullable PlayerEntity player, TooltipType type, Consumer<Text> textConsumer, CallbackInfo ci) {
		this.appendComponentTooltip(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT, context, displayComponent, textConsumer, type);
	}

	@Inject(
		method = "appendTooltip",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
			ordinal = 6
		)
	)
	private void addInfusionData(Item.TooltipContext context, TooltipDisplayComponent displayComponent, @Nullable PlayerEntity player, TooltipType type, Consumer<Text> textConsumer, CallbackInfo ci) {
		this.appendComponentTooltip(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT, context, displayComponent, textConsumer, type);
	}

	@Inject(
		method = "applyAttributeModifier",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/enchantment/EnchantmentHelper;applyAttributeModifiers(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V"
		)
	)
	private void applyAttributeModifyingComponents(AttributeModifierSlot slot, TriConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier, AttributeModifiersComponent.Display> attributeModifierConsumer, CallbackInfo ci) {
		AttributeModifyingComponent.applyModifiers((ItemStack)(Object) this, slot, attributeModifierConsumer);
	}

	@Unique
	@Override
	public final Text sevenelements$getTrueName() {
		// Do this so that any other injects to ItemStack#getName work properly with Seven Elements.
		return this.getName();
	}

	@Unique
	@Override
	public final boolean sevenelements$hasElementalGlint() {
		return JavaScriptUtil.nullishCoalesing(this.get(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE), Boolean.TRUE)
			&& (this.contains(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT) || this.sevenelements$hasAttunementGlint());
	}

	@Unique
	@Override
	public final boolean sevenelements$hasAttunementGlint() {
		return JavaScriptUtil.nullishCoalesing(this.get(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE), Boolean.TRUE)
			&& this.contains(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT)
			&& (this.contains(DataComponentTypes.EQUIPPABLE)
				|| ClassInstanceUtil.nonNullEquals(
					ClassInstanceUtil.mapOrNull(this.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT), ElementalInfusionComponent::getElement),
					ClassInstanceUtil.mapOrNull(this.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT), ElementalAttunementComponent::element)
				)
			);
	}
}
