package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

@Mixin(WitherEntity.class)
public abstract class WitherEntityMixin extends HostileEntity {
	public WitherEntityMixin(EntityType<? extends WitherEntity> entityType, World world) {
		super(entityType, world);

		throw new AssertionError();
	}

	@Shadow
	@Final
	private ServerBossBar bossBar;

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	public void setBossBarEntity(EntityType<? extends WitherEntity> entityType, World world, CallbackInfo ci) {
		this.bossBar.sevenelements$setEntity(this);
	}
}
