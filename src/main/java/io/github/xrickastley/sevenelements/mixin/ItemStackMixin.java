package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;

// Prioritized since Frozen **MUST** disable using items.
@Mixin(value = ItemStack.class, priority = Integer.MIN_VALUE)
public abstract class ItemStackMixin implements DataComponentHolder {
	@Shadow
	public abstract Item getItem();

	@Shadow
	public abstract <T extends TooltipProvider> void addToTooltip(DataComponentType<T> componentType, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type);

	@WrapOperation(
		method = "use",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/Item;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
		)
	)
	private InteractionResult frozenPreventsItemUse(Item instance, Level world, Player user, InteractionHand hand, Operation<InteractionResult> original) {
		return user.hasEffect(SevenElementsStatusEffects.FROZEN)
			? InteractionResult.FAIL
			: original.call(instance, world, user, hand);
	}

	@ModifyReturnValue(
		method = "getHoverName",
		at = @At("RETURN")
	)
	private Component modifyName(Component original) {
		final @Nullable ElementalInfusionComponent component = this.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);

		if (component == null || !component.hasElementalInfusion()) return original;

		final Element element = component.getElement();

		return Component.empty()
			.append(original)
			.append(TextHelper.noModifiers(TextHelper.color(" [" + element.getString() + "]", element.getDamageColor())));
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
}
