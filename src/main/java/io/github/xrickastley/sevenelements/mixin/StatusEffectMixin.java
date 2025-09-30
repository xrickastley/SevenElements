package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;

import io.github.xrickastley.sevenelements.interfaces.EntityAwareEffect;

import net.minecraft.entity.effect.StatusEffect;

@Mixin(StatusEffect.class)
public class StatusEffectMixin implements EntityAwareEffect {}
