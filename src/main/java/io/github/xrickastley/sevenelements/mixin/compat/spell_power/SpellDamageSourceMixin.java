package io.github.xrickastley.sevenelements.mixin.compat.spell_power;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.annotation.mixin.Local;
import io.github.xrickastley.sevenelements.compat.SpellPowerCompat;

import net.minecraft.entity.damage.DamageSource;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellSchool;

@Pseudo
@Mixin(SpellDamageSource.class)
public class SpellDamageSourceMixin {
	@ModifyReturnValue(
		method = "create(Lnet/spell_power/api/SpellSchool;Ljava/lang/String;Lnet/minecraft/entity/Entity;)Lnet/minecraft/entity/damage/DamageSource;",
		at = @At("RETURN")
	)
	private static DamageSource createElementalDamageSource(DamageSource original, @Local SpellSchool school) {
		return SpellPowerCompat.create(original, school.id);
	}
}
