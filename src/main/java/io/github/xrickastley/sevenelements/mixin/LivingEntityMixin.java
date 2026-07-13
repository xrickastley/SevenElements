package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.component.interfaces.AttributeModifyingComponent;
import io.github.xrickastley.sevenelements.effect.ElementalStatusEffect;
import io.github.xrickastley.sevenelements.element.*;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.interfaces.ILivingEntity;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypeTags;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin
	extends Entity
	implements ILivingEntity
{
	public LivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final Level world) {
		super(entityType, world);
		throw new AssertionError();
	}

	@Shadow
	public abstract double getAttributeValue(Holder<Attribute> attribute);

	@Shadow
	public abstract boolean hasEffect(Holder<MobEffect> effect);

	@Shadow
	public abstract boolean removeEffect(Holder<MobEffect> effect);

	@Unique
	private boolean sevenelements$blockedByCrystallizeShield = true; // true ONLY if ALL received DMG is blocked.

	@ModifyReturnValue(
		method = "createLivingAttributes",
		at = @At("RETURN")
	)
	private static AttributeSupplier.Builder addToLivingAttributes(AttributeSupplier.Builder builder) {
		return SevenElementsAttributes.apply(builder);
	}

	@Inject(
		method = "die",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;setPose(Lnet/minecraft/world/entity/Pose;)V"
		)
	)
	private void applyOnDeathEffects(DamageSource damageSource, CallbackInfo ci) {
		ElementalStatusEffect
			.getElementEffects()
			.forEach(this::removeEffect);

		final ElementComponent component = ElementComponent.KEY.get(this);

		component
			.getAppliedElements()
			.stream()
			.map(Functions.compose(ElementalApplication::getElement, component::getElementHolder))
			.forEach(ElementHolder::reset);
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void removeExpiredElementEffects(CallbackInfo ci) {
		final ElementComponent component = ElementComponent.KEY.get(this);

		ElementalStatusEffect
			.getElementEffects()
			.stream()
			.filter(this::hasEffect)
			.filter(Predicate.not(
				Functions.composePredicate(Holder::value, ElementalStatusEffect.class::cast, ElementalStatusEffect::getElement, component::hasElementalApplication)
			))
			.forEach(this::removeEffect);
	}

	@Inject(
		method = "onEffectsRemoved",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/effect/MobEffect;removeAttributeModifiers(Lnet/minecraft/world/entity/ai/attributes/AttributeMap;)V",
			shift = At.Shift.AFTER
		)
	)
	private void triggerEntityOnRemoved(Collection<MobEffectInstance> effects, CallbackInfo ci, @Local MobEffectInstance effect) {
		effect
			.getEffect()
			.value()
			.onRemoved((LivingEntity)(Entity) this, effect.getAmplifier());
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void applyNaturalElements(CallbackInfo ci) {
		if (!(this.level() instanceof final ServerLevel world)) return;

		if (this.sevenelements$isWet() && world.getGameRules().get(SevenElementsGameRules.HYDRO_FROM_WATER)) {
			final ElementComponent component = ElementComponent.KEY.get(this);

			component.addElementalApplication(
				Element.HYDRO,
				InternalCooldownContext
					.ofType(null, "seven-elements:natural_environment", InternalCooldownType.INTERVAL_ONLY)
					.forced(),
				1.0
			);
		} else if (this.getInBlockState().getBlock() == Blocks.FIRE && world.getGameRules().get(SevenElementsGameRules.PYRO_FROM_FIRE)) {
			final ElementComponent component = ElementComponent.KEY.get(this);

			component.addElementalApplication(
				Element.PYRO,
				InternalCooldownContext
					.ofType(null, "seven-elements:natural_environment", InternalCooldownType.INTERVAL_ONLY)
					.forced(),
				1.0
			);
		}
	}

	@Inject(
		method = "hurtServer",
		at = @At("HEAD")
	)
	private void resetCrystallizeShieldBlockedState(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		this.sevenelements$blockedByCrystallizeShield = false;
	}

	@ModifyVariable(
		method = "actuallyHurt",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"
		),
		ordinal = 0,
		argsOnly = true
	)
	private float applyCrystallizeShield$LivingEntity(float amount, @Local(argsOnly = true) DamageSource source) {
		return this.sevenelements$applyCrystallizeShield(amount, source);
	}

	@Inject(
		method = "hurtServer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void cancelIfFullyBlocked(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (this.sevenelements$blockedByCrystallizeShield) cir.setReturnValue(false);
	}

	@ModifyExpressionValue(
		method = "hurtServer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z",
			ordinal = 5
		)
	)
	private boolean preventKnockbackIfCrystallize(boolean original) {
		return this.sevenelements$modifyKnockback(original, this);
	}

	@ModifyConstant(
		method = "hurtServer",
		constant = @Constant(intValue = 20, ordinal = 0)
	)
	private int changeTimeUntilRegen(int original, @Local(argsOnly = true) DamageSource source) {
		return source.is(SevenElementsDamageTypeTags.PREVENTS_COOLDOWN_TRIGGER)
			? 10
			: original;
	}

	@Inject(
		method = "collectEquipmentChanges",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;stopLocationBasedEffects(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/ai/attributes/AttributeMap;)V"
		)
	)
	private void applyAttributeModifyingComponents$1(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir, @Local(ordinal = 0) ItemStack itemStack, @Local(ordinal = 0) EquipmentSlot slot) {
		AttributeModifyingComponent.removeModifiers((LivingEntity)(Entity) this, EquipmentSlotGroup.bySlot(slot), itemStack);
	}

	@Inject(
		method = "collectEquipmentChanges",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"
		)
	)
	private void applyAttributeModifyingComponents$2(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir, @Local(ordinal = 0) ItemStack itemStack, @Local(ordinal = 0) EquipmentSlot slot) {
		AttributeModifyingComponent.applyModifiers((LivingEntity)(Entity) this, EquipmentSlotGroup.bySlot(slot), itemStack);
	}

	@Unique
	@Override
	public void sevenelements$setBlockedByCrystallizeShield(boolean blocked) {
		this.sevenelements$blockedByCrystallizeShield = blocked;
	}

	@Unique
	@Override
	public boolean sevenelements$isWet() {
		return this.isInWaterOrRain() || this.getInBlockState().is(Blocks.BUBBLE_COLUMN);
	}

	@Unique
	protected float sevenelements$applyCrystallizeShield(float amount, DamageSource source) {
		final ElementComponent component = ElementComponent.KEY.get(this);
		final float finalAmount = amount - component.reduceCrystallizeShield(source, amount);

		if (finalAmount < amount)
			this.level().playSound(null, this.blockPosition(), SevenElementsSoundEvents.CRYSTALLIZE_SHIELD_HIT, SoundSource.PLAYERS, 1.0f, 1.0f);

		if (finalAmount <= 0 && finalAmount != amount) this.sevenelements$setBlockedByCrystallizeShield(true);

		return finalAmount;
	}

	@Unique
	protected boolean sevenelements$modifyKnockback(boolean doesKnockback, Entity entity) {
		return doesKnockback
			&& !(entity instanceof final LivingEntity livingEntity && ElementComponent.KEY.get(livingEntity).reducedCrystallizeShield());
	}
}
