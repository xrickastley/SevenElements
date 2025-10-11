package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.util.JavaScriptUtil;

import net.minecraft.entity.player.PlayerEntity;
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
	private static WindChargeEntity setElementalInfusion1(WindChargeEntity original, @Local(argsOnly = true) PlayerEntity user) {
		final @Nullable ItemStack stack = JavaScriptUtil.nullishCoalesing(
			user.getMainHandStack(),
			user.getOffHandStack()
		);

		// Unable to resolve Wind Charge stack.
		if (stack == null) return original;

		original.sevenelements$setOriginStack(stack);
		return original;
	}

	@Inject(
		method = "createEntity",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/projectile/WindChargeEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"
		)
	)
	private void setElementalInfusion2(World world, Position pos, ItemStack stack, Direction direction, CallbackInfoReturnable<ProjectileEntity> cir, @Local WindChargeEntity windCharge) {
		windCharge.sevenelements$setOriginStack(stack);
	}
}
