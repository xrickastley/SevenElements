package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.interfaces.IEnderDragonFight;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.end.EnderDragonFight;

@Mixin(EnderDragonFight.class)
public class EndDragonFightMixin implements IEnderDragonFight {
	@Shadow
	@Final
	private ServerBossEvent dragonEvent;

	@Unique
	public void sevenelements$setDragon(EnderDragon enderDragon) {
		this.dragonEvent.sevenelements$setEntity(enderDragon);
	}

	@Inject(
		method = "updateDragon",
		at = @At("HEAD")
	)
	private void updateDragonEntity(EnderDragon dragon, CallbackInfo ci) {
		if (this.dragonEvent.sevenelements$getEntity() == null) this.dragonEvent.sevenelements$setEntity(dragon);
	}
}
