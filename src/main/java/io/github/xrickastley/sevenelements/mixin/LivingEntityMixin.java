package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.Map;

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
import io.github.xrickastley.sevenelements.component.ElementComponentImpl;
import io.github.xrickastley.sevenelements.component.interfaces.AttributeModifyingComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.interfaces.ILivingEntity;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypeTags;
import io.github.xrickastley.sevenelements.util.BoxUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
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
	public abstract double getAttributeValue(EntityAttribute attribute);

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
		method = "onStatusEffectRemoved",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/attribute/AttributeContainer;I)V",
			shift = At.Shift.AFTER
		)
	)
	private void triggerEntityOnRemoved(StatusEffectInstance effect, CallbackInfo ci) {
		effect
			.getEffectType()
			.onRemoved((LivingEntity)(Entity) this, effect.getAmplifier());
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void applyNaturalElements(CallbackInfo ci) {
		if (this.isWet() && this.getWorld().getGameRules().getBoolean(SevenElementsGameRules.HYDRO_FROM_WATER)) {
			final ElementComponent component = ElementComponent.KEY.get(this);

			component.addElementalApplication(
				Element.HYDRO,
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
	private void resetCrystallizeShieldBlockedState(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
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
			target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void cancelIfFullyBlocked(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (this.sevenelements$blockedByCrystallizeShield) cir.setReturnValue(false);
	}

	@ModifyExpressionValue(
		method = "damage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/damage/DamageSource;isIn(Lnet/minecraft/registry/tag/TagKey;)Z",
			ordinal = 7
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
			target = "Lnet/minecraft/entity/attribute/AttributeContainer;removeModifiers(Lcom/google/common/collect/Multimap;)V"
		)
	)
	private void applyAttributeModifyingComponents$1(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir, @Local(ordinal = 0) ItemStack itemStack, @Local(ordinal = 0) EquipmentSlot slot) {
		AttributeModifyingComponent.removeModifiers((LivingEntity)(Entity) this, slot, itemStack);
	}

	@Inject(
		method = "getEquipmentChanges",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/attribute/AttributeContainer;addTemporaryModifiers(Lcom/google/common/collect/Multimap;)V"
		)
	)
	private void applyAttributeModifyingComponents$2(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir, @Local(ordinal = 1) ItemStack itemStack2, @Local(ordinal = 0) EquipmentSlot slot) {
		AttributeModifyingComponent.applyModifiers((LivingEntity)(Entity) this, slot, itemStack2);
	}

	@Unique
	private void sevenelements$triggerDendroCoreReactions(final DamageSource source) {
		final ElementalDamageSource eds = ElementComponentImpl.resolve(source, (LivingEntity)(Entity) this);

		final Element element = eds.getElementalApplication().getElement();

		if (element != Element.PYRO && element != Element.ELECTRO) return;

		this.getWorld()
			.getEntitiesByClass(DendroCoreEntity.class, BoxUtil.multiplyBox(this.getBoundingBox(), 2), dc -> true)
			.forEach(dc -> dc.damage(source, 1));
	}

	@Unique
	@Override
	public void sevenelements$setBlockedByCrystallizeShield(boolean blocked) {
		this.sevenelements$blockedByCrystallizeShield = blocked;
	}

	@Unique
	@Override
	public boolean sevenelements$isInCreativeMode() {
		return false;
	}

	@Unique
	protected float sevenelements$applyCrystallizeShield(float amount, DamageSource source) {
		final ElementComponent component = ElementComponent.KEY.get(this);
		final float finalAmount = amount - component.reduceCrystallizeShield(source, amount);

		if (finalAmount < amount)
			this.getWorld().playSound(null, this.getBlockPos(), SevenElementsSoundEvents.CRYSTALLIZE_SHIELD_HIT, SoundCategory.PLAYERS, 1.0f, 1.0f);

		if (finalAmount <= 0 && finalAmount != amount) this.sevenelements$setBlockedByCrystallizeShield(true);

		return finalAmount;
	}

	@Unique
	protected boolean sevenelements$modifyKnockback(boolean doesKnockback, Entity entity) {
		return doesKnockback
			&& !(entity instanceof final LivingEntity livingEntity && ElementComponent.KEY.get(livingEntity).reducedCrystallizeShield());
	}
}
