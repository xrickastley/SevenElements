package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {
	private AbstractArrowMixin(EntityType<? extends Projectile> entityType, Level world) {
		super(entityType, world);

		throw new AssertionError();
	}

	@Inject(
		method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
		at = @At("CTOR_HEAD")
	)
	private void setElementalInfusion1(EntityType<?> type, LivingEntity owner, Level world, ItemStack stack, ItemStack shotFrom, CallbackInfo ci) {
		this.sevenelements$setOriginStack(shotFrom);
	}

	@Inject(
		method = "<init>(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
		at = @At("CTOR_HEAD")
	)
	private void setElementalInfusion2(EntityType<?> type, double x, double y, double z, Level world, ItemStack stack, ItemStack weapon, CallbackInfo ci) {
		this.sevenelements$setOriginStack(stack);
	}
}
