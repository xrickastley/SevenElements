package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.interfaces.IEnderDragonFight;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EndDragonFight;

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin
	extends Mob
	implements Enemy
{
	public EnderDragonMixin(EntityType<? extends EnderDragon> entityType, Level world) {
		super(EntityType.ENDER_DRAGON, world);

		throw new AssertionError();
	}

	@Inject(
		method = "setDragonFight",
		at = @At("HEAD")
	)
	private void addEnderDragonEntityToFight(EndDragonFight fight, CallbackInfo ci) {
		ClassInstanceUtil.ifPresentMapped(
			fight,
			IEnderDragonFight.class::cast,
			Functions.withArgument(IEnderDragonFight::sevenelements$setDragon, ClassInstanceUtil.cast(this))
		);
	}

	@Inject(
		method = "aiStep",
		at = @At("HEAD")
	)
	private void sendDragonUpdates(CallbackInfo ci) {
		if (!(this.level() instanceof final ServerLevel world)) return;

		ClassInstanceUtil.ifPresentMapped(
			world.getDragonFight(),
			IEnderDragonFight.class::cast,
			Functions.withArgument(IEnderDragonFight::sevenelements$setDragon, ClassInstanceUtil.cast(this))
		);
	}

	@Inject(
		method = "aiStep",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/server/level/ServerLevel;getDragonFight()Lnet/minecraft/world/level/dimension/end/EndDragonFight;"
		)
	)
	private void setDragonOnFightUpdate(CallbackInfo ci, @Local EndDragonFight enderDragonFight) {
		ClassInstanceUtil.ifPresentMapped(
			enderDragonFight,
			IEnderDragonFight.class::cast,
			Functions.withArgument(IEnderDragonFight::sevenelements$setDragon, ClassInstanceUtil.cast(this))
		);
	}
}
