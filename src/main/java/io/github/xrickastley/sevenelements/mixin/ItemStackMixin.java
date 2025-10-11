package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext.Builder;
import io.github.xrickastley.sevenelements.element.InternalCooldownTag;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.component.ComponentHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

// Prioritized since Frozen **MUST** disable using items.
@Mixin(value = ItemStack.class, priority = Integer.MIN_VALUE)
public abstract class ItemStackMixin implements ComponentHolder {
	@Shadow
	public abstract Item getItem();

	@WrapOperation(
		method = "use",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"
		)
	)
	private ActionResult frozenPreventsItemUse(Item instance, World world, PlayerEntity user, Hand hand, Operation<ActionResult> original) {
		return user.hasStatusEffect(SevenElementsStatusEffects.FROZEN)
			? ActionResult.FAIL
			: original.call(instance, world, user, hand);
	}

	@ModifyReturnValue(
		method = "getName",
		at = @At("RETURN")
	)
	private Text modifyName(Text original) {
		final @Nullable ElementalInfusionComponent component = this.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);

		if (component == null || !component.hasElementalInfusion()) return original;

		final Element element = component.getElement();

		return Text.empty()
			.append(original)
			.append(TextHelper.noModifiers(TextHelper.color(" [" + element.getString() + "]", element.getDamageColor())));
	}

	@Inject(
		method = "getTooltip",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
			ordinal = 3
		)
	)
	private void addInfusionData(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType _type, CallbackInfoReturnable<List<Text>> cir, @Local List<Text> texts) {
		final @Nullable ElementalInfusionComponent component = this.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);

		if (component == null || !component.hasElementalInfusion()) return;

		final Builder builder = component.internalCooldown();

		texts.add(
			Text.empty()
				.append(Text.translatable("item.seven-elements.components.infusion.infusion").formatted(Formatting.WHITE))
				.append(ElementalApplication.Builder.getText(component.elementalInfusion()))
		);



		@Nullable InternalCooldownTag tag = ClassInstanceUtil.mapOrNull(builder, Builder::getTag);

		final Text tagText = tag != null
			? tag.getText(Formatting.DARK_GRAY)
			: Text.literal("none").formatted(Formatting.RED);

		texts.add(
			Text.empty()
				.append(Text.translatable("item.seven-elements.components.infusion.tag").formatted(Formatting.WHITE))
				.append(tagText)
		);



		final InternalCooldownType type = JavaScriptUtil.nullishCoalesing(
			ClassInstanceUtil.mapOrNull(builder, Builder::getType),
			InternalCooldownType.DEFAULT
		);

		texts.add(
			Text.empty()
				.append(Text.translatable("item.seven-elements.components.infusion.type").formatted(Formatting.WHITE))
				.append(type.getText(true).formatted(Formatting.DARK_GRAY))
		);
	}
}
