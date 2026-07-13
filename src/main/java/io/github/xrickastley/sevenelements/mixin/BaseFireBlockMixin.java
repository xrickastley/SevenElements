package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BaseFireBlock;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {
	@Inject(
		method = "fireIgnite",
		at = @At("HEAD")
	)
	private static void applyPyroOnCollision(Entity entity, CallbackInfo ci) {
		if (entity.level() instanceof final ServerLevel world && world.getGameRules().get(SevenElementsGameRules.PYRO_FROM_FIRE) && entity instanceof final LivingEntity livingEntity) {
			final ElementComponent component = ElementComponent.KEY.get(livingEntity);

			component.addElementalApplication(
				Element.PYRO,
				InternalCooldownContext
					.ofType(null, "seven-elements:natural_environment", InternalCooldownType.INTERVAL_ONLY)
					.forced(),
				1.0
			);
		}
	}
}
