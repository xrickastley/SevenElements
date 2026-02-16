package io.github.xrickastley.sevenelements.mixin.compat.taxfreelevels;

import io.github.fourmisain.taxfreelevels.TaxFreeLevels;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

import io.github.xrickastley.sevenelements.screen.ElementalInfusionScreenHandler;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ElementalInfusionScreenHandler.class)
public class ElementalInfusionScreenHandlerMixin {
	@Redirect(
		method = "infuse",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerPlayerEntity;addExperienceLevels(I)V"
		)
	)
	public void applyTaxFreeLevels(ServerPlayerEntity serverPlayer, int level) {
		TaxFreeLevels.applyFlattenedXpCost(serverPlayer, -level);
	}
}
