package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.xrickastley.sevenelements.interfaces.IBossBar;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;

@Mixin(BossBar.class)
public abstract class BossBarMixin implements IBossBar {
	@Unique
	protected LivingEntity sevenelements$entity;

	@Unique
	@Override
	public void sevenelements$setEntity(LivingEntity entity) {
		this.sevenelements$entity = entity;
	}

	@Unique
	public LivingEntity sevenelements$getEntity() {
		return this.sevenelements$entity;
	}
}
