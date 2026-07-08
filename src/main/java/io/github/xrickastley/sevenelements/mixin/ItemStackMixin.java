package io.github.xrickastley.sevenelements.mixin;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.component.ElementalAttunementComponent;
import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.component.interfaces.AttributeModifyingComponent;
import io.github.xrickastley.sevenelements.component.interfaces.ElementModifyingComponent;
import io.github.xrickastley.sevenelements.component.interfaces.ElementModifyingComponentImpl;
import io.github.xrickastley.sevenelements.component.interfaces.TooltipProvider;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.interfaces.IItemStack;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.TextHelper;
import io.github.xrickastley.sevenelements.util.Util;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

// Prioritized since Frozen **MUST** disable using items.
@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IItemStack {
	@Shadow
	public abstract Item getItem();

	@Shadow
	public abstract Text getName();

	@Shadow
	public abstract boolean hasGlint();

	@ModifyReturnValue(
		method = "getName",
		at = @At("RETURN")
	)
	private Text modifyName(Text original) {
		final @Nullable ElementalInfusionComponent component = ElementalInfusionComponent.get((ItemStack)(Object) this);

		if (
			component == null
			|| !component.hasElementalInfusion()
			|| Util.isCalledBy(ItemStack.class, "sevenelements$getTrueName", 1)
		) return original;

		final Element element = component.getElement();

		final String symbols = ElementModifyingComponentImpl.getComponentsOf(ClassInstanceUtil.cast(this))
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
			target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z",
			ordinal = 2,
			shift = At.Shift.BEFORE
		)
	)
	private void addAttunementData(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, @Local List<Text> list) {
		TooltipProvider.appendTooltip(ElementalAttunementComponent.KEY, (ItemStack)(Object) this, player, context, list::add);
	}

	@Inject(
		method = "getTooltip",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
			ordinal = 18,
			shift = At.Shift.AFTER
		)
	)
	private void addInfusionData(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, @Local List<Text> list) {
		TooltipProvider.appendTooltip(ElementalInfusionComponent.KEY, (ItemStack)(Object) this, player, context, list::add);
	}

	@ModifyReturnValue(
		method = "getAttributeModifiers",
		at = @At("RETURN")
	)
	private Multimap<EntityAttribute, EntityAttributeModifier> applyAttributeModifyingComponents(Multimap<EntityAttribute, EntityAttributeModifier> original, @Local EquipmentSlot slot) {
		final Multimap<EntityAttribute, EntityAttributeModifier> attributes = LinkedHashMultimap.create();

		attributes.putAll(original);
		attributes.putAll(AttributeModifyingComponent.getModifiers(slot, (ItemStack)(Object) this));

		return attributes;
	}

	@ModifyExpressionValue(
		method = "getTooltip",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier$Operation;getId()I"
		)
	)
	private int modifyIdForMultiplicativeLikeAttributes(int original, @Local Map.Entry<EntityAttribute, EntityAttributeModifier> entry) {
		return SevenElementsAttributes.isMultiplicativeLikeAttribute(entry.getKey())
			? 1
			: original;
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
		return ElementalInfusionComponent.hasInfusion(ClassInstanceUtil.cast(this))
			|| this.sevenelements$hasAttunementGlint();
	}

	@Unique
	@Override
	public final boolean sevenelements$hasAttunementGlint() {
		return ElementalAttunementComponent.hasAttunement(ClassInstanceUtil.cast(this))
			&& (this.getItem() instanceof ArmorItem
				|| ClassInstanceUtil.nonNullEquals(
					ClassInstanceUtil.mapOrNull(ElementalInfusionComponent.get(ClassInstanceUtil.cast(this)), ElementalInfusionComponent::getElement),
					ClassInstanceUtil.mapOrNull(ElementalAttunementComponent.get(ClassInstanceUtil.cast(this)), ElementalAttunementComponent::element)
				)
			);
	}
}
