package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

// Prioritized since Frozen **MUST** disable using items.
@Mixin(value = ItemStack.class, priority = Integer.MIN_VALUE)
public abstract class ItemStackMixin {
	@Shadow
	public abstract Item getItem();

	@WrapOperation(
		method = "use",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"
		)
	)
	private TypedActionResult<ItemStack> frozenPreventsItemUse(Item instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original) {
		ItemStack handStack = user.getStackInHand(hand);

		return user.hasStatusEffect(SevenElementsStatusEffects.FROZEN)
			? TypedActionResult.fail(handStack)
			: original.call(instance, world, user, hand);
	}

	@ModifyReturnValue(
		method = "getName",
		at = @At("RETURN")
	)
	private Text modifyName(Text original) {
		final @Nullable ElementalInfusionComponent component = ElementalInfusionComponent.get((ItemStack)(Object) this);

		if (component == null || !component.hasElementalInfusion()) return original;

		final Element element = component.getElement();

		return Text.empty()
			.append(original)
			.append(TextHelper.noModifiers(TextHelper.color(" [" + element.getString() + "]", element.getDamageColor())));
	}
}
