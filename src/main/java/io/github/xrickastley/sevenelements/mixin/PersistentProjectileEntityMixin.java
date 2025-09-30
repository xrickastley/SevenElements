package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin extends ProjectileEntity {
	private PersistentProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);

		throw new AssertionError();
	}

	@Inject(
		method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)V",
		at = @At("CTOR_HEAD")
	)
	private void setElementalInfusion1(EntityType<?> type, LivingEntity owner, World world, ItemStack stack, ItemStack shotFrom, CallbackInfo ci) {
		this.sevenelements$setOriginStack(shotFrom);
	}

	@Inject(
		method = "<init>(Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)V",
		at = @At("CTOR_HEAD")
	)
	private void setElementalInfusion2(EntityType<?> type, double x, double y, double z, World world, ItemStack stack, ItemStack weapon, CallbackInfo ci) {
		this.sevenelements$setOriginStack(stack);
	}
}
