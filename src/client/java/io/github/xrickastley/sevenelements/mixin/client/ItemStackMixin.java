package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext.Builder;
import io.github.xrickastley.sevenelements.element.InternalCooldownTag;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Inject(
		method = "getTooltip",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
			ordinal = 16,
			shift = At.Shift.BEFORE
		)
	)
	private void addInfusionData(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, @Local List<Text> texts) {
		final @Nullable ElementalInfusionComponent component = ElementalInfusionComponent.get((ItemStack)(Object) this);

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
