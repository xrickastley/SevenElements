package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.interfaces.DamageSourceWrapper;
import io.github.xrickastley.sevenelements.interfaces.IPlayerEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin
	extends LivingEntityMixin
	implements IPlayerEntity
{
	public PlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(EntityType.PLAYER, world);
		throw new AssertionError();
	}

	@Unique
	private Set<DamageSource> sevenelements$critDamageSources = new HashSet<>();

	@Unique
	@Override
	public boolean sevenelements$isCrit(DamageSource source) {
		return this.sevenelements$critDamageSources != null
			&& DamageSourceWrapper.getDamageSources(source)
				.anyMatch(this.sevenelements$critDamageSources::contains);
	}

	@ModifyVariable(
		method = "applyDamage",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/entity/player/PlayerEntity;modifyAppliedDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"
		),
		ordinal = 0,
		argsOnly = true
	)
	private float applyCrystallizeShield$PlayerEntity(float amount, @Local(argsOnly = true) DamageSource source) {
		return this.sevenelements$applyCrystallizeShield(amount, source);
	}

	// why are there two separate knockbacks :sob:
	@Definition(id = "k", local = @Local(type = float.class, ordinal = 5))
	@Expression("k > 0.0")
	@ModifyExpressionValue(
		method = "attack",
		at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private boolean preventKnockbackIfCrystallize(boolean original, @Local(argsOnly = true) Entity entity) {
		return this.sevenelements$modifyKnockback(original, entity);
	}

	@Definition(id = "bl3", local = @Local(type = boolean.class, ordinal = 2))
	@Expression("bl3")
	@ModifyVariable(
		method = "attack",
		at = @At("MIXINEXTRAS:EXPRESSION"),
		ordinal = 2
	)
	private boolean applyCriticalRateAttribute(boolean bl3) {
		return bl3 || this.getRandom().nextDouble() < (this.getAttributeValue(SevenElementsAttributes.CRITICAL_RATE) / 100);
	}

	@ModifyExpressionValue(
		method = "attack",
		at = @At(
			value = "CONSTANT",
			args = "floatValue=1.5"
		)
	)
	private float applyCriticalDamageAttribute(float original) {
		return original + (float) (this.getAttributeValue(SevenElementsAttributes.CRITICAL_DAMAGE) / 100);
	}

	@ModifyVariable(
		method = "damageShield",
		at = @At("HEAD"),
		argsOnly = true
	)
	private float applyShieldStrengthAttributeToNormalShield(float amount) {
		return (float) (amount / (1 + (this.getAttributeValue(SevenElementsAttributes.SHIELD_STRENGTH) / 100)));
	}

	@ModifyArg(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
		),
		index = 0
	)
	private DamageSource checkForCritMain(DamageSource source, @Local(ordinal = 2) boolean crit) {
		return this.sevenelements$addCritDamageSource(source, crit);
	}

	@ModifyArg(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
		),
		index = 0
	)
	private DamageSource checkForCritSweep(DamageSource source, @Local(ordinal = 2) boolean crit) {
		return this.sevenelements$addCritDamageSource(source, crit);
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void removeCritDS(CallbackInfo ci) {
		if (sevenelements$critDamageSources != null)
			sevenelements$critDamageSources.clear();
		else
			sevenelements$critDamageSources = new HashSet<>();
	}

	private DamageSource sevenelements$addCritDamageSource(DamageSource source, boolean crit) {
		if (sevenelements$critDamageSources == null) sevenelements$critDamageSources = new HashSet<>();

		if (crit) sevenelements$critDamageSources.add(source);

		return source;
	}
}
