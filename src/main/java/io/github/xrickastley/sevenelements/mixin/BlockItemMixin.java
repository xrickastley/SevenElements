package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;

// Prioritized since Frozen **MUST** disable actions.
@Mixin(value = BlockItem.class, priority = Integer.MIN_VALUE)
public class BlockItemMixin {
	@Inject(
		method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void frozenPreventsItemUse(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
		final PlayerEntity player = context.getPlayer();

		if (player != null && player.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			cir.setReturnValue(ActionResult.FAIL);
	}
}
