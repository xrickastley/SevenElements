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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.component.interfaces.AttributeModifyingComponent;
import io.github.xrickastley.sevenelements.effect.ElementalStatusEffect;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementHolder;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.interfaces.ILivingEntity;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypeTags;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.block.Blocks;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin
	extends Entity
	implements ILivingEntity
{
	public LivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final World world) {
		super(entityType, world);
		throw new AssertionError();
	}

	@Shadow
	public abstract double getAttributeValue(RegistryEntry<EntityAttribute> attribute);

	@Shadow
	public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);

	@Shadow
	public abstract boolean removeStatusEffect(RegistryEntry<StatusEffect> effect);

	@Unique
	private boolean sevenelements$blockedByCrystallizeShield = true; // true ONLY if ALL received DMG is blocked.

	@ModifyReturnValue(
		method = "createLivingAttributes",
		at = @At("RETURN")
	)
	private static DefaultAttributeContainer.Builder addToLivingAttributes(DefaultAttributeContainer.Builder builder) {
		return SevenElementsAttributes.apply(builder);
	}

	@Inject(
		method = "onDeath",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;setPose(Lnet/minecraft/entity/EntityPose;)V"
		)
	)
	private void applyOnDeathEffects(DamageSource damageSource, CallbackInfo ci) {
		ElementalStatusEffect
			.getElementEffects()
			.forEach(this::removeStatusEffect);

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
			.filter(this::hasStatusEffect)
			.filter(Predicate.not(
				Functions.composePredicate(RegistryEntry::value, ElementalStatusEffect.class::cast, ElementalStatusEffect::getElement, component::hasElementalApplication)
			))
			.forEach(this::removeStatusEffect);
	}

	@Inject(
		method = "onStatusEffectsRemoved",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved(Lnet/minecraft/entity/attribute/AttributeContainer;)V",
			shift = At.Shift.AFTER
		)
	)
	private void triggerEntityOnRemoved(Collection<StatusEffectInstance> effects, CallbackInfo ci, @Local StatusEffectInstance effect) {
		effect
			.getEffectType()
			.value()
			.onRemoved((LivingEntity)(Entity) this, effect.getAmplifier());
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void applyNaturalElements(CallbackInfo ci) {
		if (!(this.getEntityWorld() instanceof final ServerWorld world)) return;

		if (this.sevenelements$isWet() && world.getGameRules().getValue(SevenElementsGameRules.HYDRO_FROM_WATER)) {
			final ElementComponent component = ElementComponent.KEY.get(this);

			component.addElementalApplication(
				Element.HYDRO,
				InternalCooldownContext
					.ofType(null, "seven-elements:natural_environment", InternalCooldownType.INTERVAL_ONLY)
					.forced(),
				1.0
			);
		} else if (this.getBlockStateAtPos().getBlock() == Blocks.FIRE && world.getGameRules().getValue(SevenElementsGameRules.PYRO_FROM_FIRE)) {
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
		method = "damage",
		at = @At("HEAD")
	)
	private void resetCrystallizeShieldBlockedState(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		this.sevenelements$blockedByCrystallizeShield = false;
	}

	@ModifyVariable(
		method = "applyDamage",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/entity/LivingEntity;modifyAppliedDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"
		),
		ordinal = 0,
		argsOnly = true
	)
	private float applyCrystallizeShield$LivingEntity(float amount, @Local(argsOnly = true) DamageSource source) {
		return this.sevenelements$applyCrystallizeShield(amount, source);
	}

	@Inject(
		method = "damage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void cancelIfFullyBlocked(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (this.sevenelements$blockedByCrystallizeShield) cir.setReturnValue(false);
	}

	@ModifyExpressionValue(
		method = "damage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/damage/DamageSource;isIn(Lnet/minecraft/registry/tag/TagKey;)Z",
			ordinal = 5
		)
	)
	private boolean preventKnockbackIfCrystallize(boolean original) {
		return this.sevenelements$modifyKnockback(original, this);
	}

	@ModifyConstant(
		method = "damage",
		constant = @Constant(intValue = 20, ordinal = 0)
	)
	private int changeTimeUntilRegen(int original, @Local(argsOnly = true) DamageSource source) {
		return source.isIn(SevenElementsDamageTypeTags.PREVENTS_COOLDOWN_TRIGGER)
			? 10
			: original;
	}

	@Inject(
		method = "getEquipmentChanges",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;onEquipmentRemoved(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/entity/attribute/AttributeContainer;)V"
		)
	)
	private void applyAttributeModifyingComponents$1(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir, @Local(ordinal = 0) ItemStack itemStack, @Local(ordinal = 0) EquipmentSlot slot) {
		AttributeModifyingComponent.removeModifiers((LivingEntity)(Entity) this, AttributeModifierSlot.forEquipmentSlot(slot), itemStack);
	}

	@Inject(
		method = "getEquipmentChanges",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/ItemStack;applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"
		)
	)
	private void applyAttributeModifyingComponents$2(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir, @Local(ordinal = 0) ItemStack itemStack, @Local(ordinal = 0) EquipmentSlot slot) {
		AttributeModifyingComponent.applyModifiers((LivingEntity)(Entity) this, AttributeModifierSlot.forEquipmentSlot(slot), itemStack);
	}

	@Unique
	@Override
	public void sevenelements$setBlockedByCrystallizeShield(boolean blocked) {
		this.sevenelements$blockedByCrystallizeShield = blocked;
	}

	@Unique
	@Override
	public boolean sevenelements$isWet() {
		return this.isTouchingWaterOrRain() || this.getBlockStateAtPos().isOf(Blocks.BUBBLE_COLUMN);
	}

	@Unique
	protected float sevenelements$applyCrystallizeShield(float amount, DamageSource source) {
		final ElementComponent component = ElementComponent.KEY.get(this);
		final float finalAmount = amount - component.reduceCrystallizeShield(source, amount);

		if (finalAmount < amount)
			this.getEntityWorld().playSound(null, this.getBlockPos(), SevenElementsSoundEvents.CRYSTALLIZE_SHIELD_HIT, SoundCategory.PLAYERS, 1.0f, 1.0f);

		if (finalAmount <= 0 && finalAmount != amount) this.sevenelements$setBlockedByCrystallizeShield(true);

		return finalAmount;
	}

	@Unique
	protected boolean sevenelements$modifyKnockback(boolean doesKnockback, Entity entity) {
		return doesKnockback
			&& !(entity instanceof final LivingEntity livingEntity && ElementComponent.KEY.get(livingEntity).reducedCrystallizeShield());
	}
}
