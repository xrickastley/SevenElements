package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.interfaces.IEnderDragonFight;

import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;

@Mixin(EnderDragonFight.class)
public class EnderDragonFightMixin implements IEnderDragonFight {
	@Shadow
	@Final
	private ServerBossBar bossBar;

	@Unique
	public void sevenelements$setDragon(EnderDragonEntity enderDragon) {
		this.bossBar.sevenelements$setEntity(enderDragon);
	}

	@Inject(
		method = "updateFight",
		at = @At("HEAD")
	)
	private void updateDragonEntity(EnderDragonEntity dragon, CallbackInfo ci) {
		if (this.bossBar.sevenelements$getEntity() == null) this.bossBar.sevenelements$setEntity(dragon);
	}
}
