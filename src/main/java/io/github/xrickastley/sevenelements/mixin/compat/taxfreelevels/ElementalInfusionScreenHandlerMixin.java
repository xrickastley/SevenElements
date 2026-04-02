package io.github.xrickastley.sevenelements.mixin.compat.taxfreelevels;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import io.github.fourmisain.taxfreelevels.TaxFreeLevels;
import io.github.xrickastley.sevenelements.screen.ElementalInfusionScreenHandler;

import net.minecraft.server.level.ServerPlayer;

@Mixin(ElementalInfusionScreenHandler.class)
public class ElementalInfusionScreenHandlerMixin {
	@Redirect(
		method = "infuse",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;giveExperienceLevels(I)V"
		)
	)
	public void applyTaxFreeLevels(ServerPlayer serverPlayer, int level) {
		TaxFreeLevels.applyFlattenedXpCost(serverPlayer, -level);
	}
}
