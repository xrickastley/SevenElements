package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WindChargeItem;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

@Mixin(WindChargeItem.class)
public class WindChargeItemMixin {
	@ModifyReturnValue(
		method = "method_61665",
		at = @At("RETURN")
	)
	private static WindChargeEntity setElementalInfusion$1(WindChargeEntity original, @Local(argsOnly = true) ItemStack stack) {
		original.sevenelements$setOriginStack(stack);
		original.sevenelements$setProjectileStack(stack);

		return original;
	}

	@Inject(
		method = "createEntity",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/projectile/WindChargeEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"
		)
	)
	private void setElementalInfusion$2(World world, Position pos, ItemStack stack, Direction direction, CallbackInfoReturnable<ProjectileEntity> cir, @Local WindChargeEntity windCharge) {
		windCharge.sevenelements$setOriginStack(stack);
		windCharge.sevenelements$setProjectileStack(stack);
	}
}
