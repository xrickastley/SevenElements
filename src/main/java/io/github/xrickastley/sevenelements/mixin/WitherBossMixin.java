package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin extends Monster {
	public WitherBossMixin(EntityType<? extends WitherBoss> entityType, Level world) {
		super(entityType, world);

		throw new AssertionError();
	}

	@Shadow
	@Final
	private ServerBossEvent bossEvent;

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	public void setBossBarEntity(EntityType<? extends WitherBoss> entityType, Level world, CallbackInfo ci) {
		this.bossEvent.sevenelements$setEntity(this);
	}
}
