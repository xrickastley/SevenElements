package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;

@Mixin(value = BlockItem.class, priority = Integer.MIN_VALUE)
public class BlockItemMixin {
	@Inject(
		method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable actions.
	)
	private void frozenPreventsItemUse(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		final Player player = context.getPlayer();

		if (player != null && player.hasEffect(SevenElementsStatusEffects.FROZEN))
			cir.setReturnValue(InteractionResult.FAIL);
	}
}
