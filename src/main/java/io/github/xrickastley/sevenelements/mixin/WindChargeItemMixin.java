package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.util.JavaScriptUtil;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.level.Level;

@Mixin(WindChargeItem.class)
public class WindChargeItemMixin {
	@ModifyReturnValue(
		method = "lambda$use$0",
		at = @At("RETURN")
	)
	private static WindCharge setElementalInfusion1(WindCharge original, @Local(argsOnly = true) Player user) {
		final @Nullable ItemStack stack = JavaScriptUtil.nullishCoalesing(
			user.getMainHandItem(),
			user.getOffhandItem()
		);

		// Unable to resolve Wind Charge stack.
		if (stack == null) return original;

		original.sevenelements$setOriginStack(stack);
		return original;
	}

	@Inject(
		method = "asProjectile",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/hurtingprojectile/windcharge/WindCharge;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
		)
	)
	private void setElementalInfusion2(Level world, Position pos, ItemStack stack, Direction direction, CallbackInfoReturnable<Projectile> cir, @Local WindCharge windCharge) {
		windCharge.sevenelements$setOriginStack(stack);
	}
}
