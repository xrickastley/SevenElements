package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;

import io.github.xrickastley.sevenelements.interfaces.EntityAwareEffect;

import net.minecraft.world.effect.MobEffect;

@Mixin(MobEffect.class)
public class MobEffectMixin implements EntityAwareEffect {}
