package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.component.interfaces.AttributeModifyingComponent;
import io.github.xrickastley.sevenelements.component.interfaces.ElementModifyingComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.TextHelper;
import io.github.xrickastley.sevenelements.util.Util;

import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder {
	@Shadow
	private <T extends TooltipAppender> void appendTooltip(ComponentType<T> componentType, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type) { throw new AssertionError(); }

	@ModifyReturnValue(
		method = "getName",
		at = @At("RETURN")
	)
	private Text modifyName(Text original) {
		final @Nullable ElementalInfusionComponent component = this.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);

		if (component == null || !component.hasElementalInfusion() || Util.isCalledBy("net.minecraft.client.gui.screen.ingame.AnvilScreen", "onSlotUpdate", 1) || Util.isCalledBy(AnvilScreenHandler.class, "updateResult", 2)) return original;

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
		method = "getTooltip",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/ItemStack;appendTooltip(Lnet/minecraft/component/ComponentType;Lnet/minecraft/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/item/tooltip/TooltipType;)V",
			ordinal = 1,
			shift = At.Shift.AFTER
		)
	)
	private void addAttunementData(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir, @Local Consumer<Text> consumer) {
		this.appendTooltip(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT, context, consumer, type);
	}

	@Inject(
		method = "getTooltip",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
			ordinal = 3
		)
	)
	private void addInfusionData(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir, @Local List<Text> list) {
		this.appendTooltip(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT, context, list::add, type);
	}

	@Inject(
		method = "applyAttributeModifier",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/enchantment/EnchantmentHelper;applyAttributeModifiers(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V"
		)
	)
	private void applyAttributeModifyingComponents(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
		AttributeModifyingComponent.applyModifiers((ItemStack)(Object) this, slot, attributeModifierConsumer);
	}

	@ModifyExpressionValue(
		method = "appendAttributeModifierTooltip",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier$Operation;getId()I"
		)
	)
	private int modifyIdForMultiplicativeLikeAttributes(int original, @Local(argsOnly = true) RegistryEntry<EntityAttribute> attribute) {
		return SevenElementsAttributes.isMultiplicativeLikeAttribute(attribute)
			? 1
			: original;
	}
}
