package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.xrickastley.sevenelements.interfaces.IDamageSource;

import net.minecraft.entity.damage.DamageSource;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements IDamageSource {
	@Unique
	private boolean sevenelements$displayDamage = true;

	@Override
	public void sevenelements$shouldDisplayDamage(boolean display) {
		this.sevenelements$displayDamage = display;
	}

	@Override
	public boolean sevenelements$displayDamage() {
		return sevenelements$displayDamage;
	}
}
