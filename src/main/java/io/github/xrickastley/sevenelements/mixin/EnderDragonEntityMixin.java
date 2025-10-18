package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.interfaces.IEnderDragonFight;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin
	extends MobEntity
	implements Monster
{
	public EnderDragonEntityMixin(EntityType<? extends EnderDragonEntity> entityType, World world) {
		super(EntityType.ENDER_DRAGON, world);

		throw new AssertionError();
	}

	@Inject(
		method = "setFight",
		at = @At("HEAD")
	)
	private void addEnderDragonEntityToFight(EnderDragonFight fight, CallbackInfo ci) {
		((IEnderDragonFight) fight).sevenelements$setDragon(ClassInstanceUtil.cast(this));

	}

	@Inject(
		method = "tickMovement",
		at = @At("HEAD")
	)
	private void sendDragonUpdates(CallbackInfo ci) {
		if (!(this.getEntityWorld() instanceof final ServerWorld world)) return;

		((IEnderDragonFight) world.getEnderDragonFight())
			.sevenelements$setDragon(ClassInstanceUtil.cast(this));
	}

	@Inject(
		method = "tickMovement",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/server/world/ServerWorld;getEnderDragonFight()Lnet/minecraft/entity/boss/dragon/EnderDragonFight;"
		)
	)
	private void setDragonOnFightUpdate(CallbackInfo ci, @Local EnderDragonFight enderDragonFight) {
		((IEnderDragonFight) enderDragonFight)
			.sevenelements$setDragon(ClassInstanceUtil.cast(this));
	}
}
