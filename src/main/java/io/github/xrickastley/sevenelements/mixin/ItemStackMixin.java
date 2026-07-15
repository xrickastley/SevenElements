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

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder, IItemStack {
	@Shadow
	public abstract <T extends TooltipProvider> void addToTooltip(DataComponentType<T> componentType, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type);

	@Shadow
	public abstract Item getItem();

	@Shadow
	public abstract Component getHoverName();

	@ModifyReturnValue(
		method = "getHoverName",
		at = @At("RETURN")
	)
	private Component modifyName(Component original) {
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
				ClassInstanceUtil.ifInstanceOf(component2.value(), ElementModifyingComponent.class, mapper)
			)
			.filter(Functions.withArgument(ElementModifyingComponent::shouldModify, component))
			.map(emc -> " " + emc.getSymbol().getString())
			.collect(Collectors.joining());

		return Component.empty()
			.append(original)
			.append(TextHelper.noModifiers(TextHelper.color(" [" + element.getString() + symbols + "]", element.getDamageColor())));
	}

	@Inject(
		method = "addDetailsToTooltip",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
			ordinal = 6
		)
	)
	private void addInfusionData(Item.TooltipContext context, TooltipDisplay displayComponent, @Nullable Player player, TooltipFlag type, Consumer<Component> textConsumer, CallbackInfo ci) {
		this.addToTooltip(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT, context, displayComponent, textConsumer, type);
	}

	@Inject(
		method = "forEachModifier",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;forEachModifier(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V"
		)
	)
	private void applyAttributeModifyingComponents(EquipmentSlotGroup slot, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> attributeModifierConsumer, CallbackInfo ci) {
		AttributeModifyingComponent.applyModifiers((ItemStack)(Object) this, slot, attributeModifierConsumer);
	}

	@Unique
	@Override
	public final Component sevenelements$getTrueName() {
		// Do this so that any other injects to ItemStack#getName work properly with Seven Elements.
		return this.getHoverName();
	}

	@Unique
	@Override
	public final boolean sevenelements$hasElementalGlint() {
		return JavaScriptUtil.nullishCoalesing(this.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE), Boolean.TRUE)
			&& (this.has(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT) || this.sevenelements$hasAttunementGlint());
	}

	@Unique
	@Override
	public final boolean sevenelements$hasAttunementGlint() {
		return JavaScriptUtil.nullishCoalesing(this.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE), Boolean.TRUE)
			&& this.has(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT)
			&& (this.has(DataComponents.EQUIPPABLE)
				|| ClassInstanceUtil.nonNullEquals(
					ClassInstanceUtil.mapOrNull(this.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT), ElementalInfusionComponent::getElement),
					ClassInstanceUtil.mapOrNull(this.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT), ElementalAttunementComponent::element)
				)
			);
	}
}
