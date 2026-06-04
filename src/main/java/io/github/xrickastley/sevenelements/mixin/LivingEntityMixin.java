package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import java.util.Collection;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.component.interfaces.AttributeModifyingComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.interfaces.ILivingEntity;
import io.github.xrickastley.sevenelements.interfaces.IPlayerEntity;
import io.github.xrickastley.sevenelements.networking.ShowElementalDamageS2CPayload;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypeTags;
import io.github.xrickastley.sevenelements.util.BoxUtil;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin
	extends Entity
	implements ILivingEntity
{
	public LivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final Level world) {
		super(entityType, world);
		throw new AssertionError();
	}

	@Unique
	private float sevenelements$subdamage;
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
	private float applyCrystallizeShield(float amount, @Local(argsOnly = true) DamageSource source) {
		final ElementComponent component = ElementComponent.KEY.get(this);
		final float finalAmount = amount - component.reduceCrystallizeShield(source, amount);

		if (finalAmount < amount)
			this.level().playSound(null, this.blockPosition(), SevenElementsSoundEvents.CRYSTALLIZE_SHIELD_HIT, SoundSource.PLAYERS, 1.0f, 1.0f);

		if (finalAmount <= 0) this.sevenelements$blockedByCrystallizeShield = true;

		return finalAmount;
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
	private boolean preventKnockbackIfCrystallize(boolean original, @Local(argsOnly = true) DamageSource source, @Share("sevenelements$hasCrystallizeShield") LocalBooleanRef hasCrystallizeShield) {
		final ElementComponent component = ElementComponent.KEY.get(this);

		return original || component.reducedCrystallizeShield();
	}

	@Inject(
		method = "actuallyHurt",
		at = @At("TAIL")
	)
	private void elementDamageHandler(ServerLevel world, DamageSource source, float _amount, CallbackInfo ci, @Local(ordinal = 1) float amount) {
		this.sevenelements$triggerDendroCoreReactions(world, source);

		if (!source.sevenelements$displayDamage()) return;

		final ElementalDamageSource eds = source instanceof final ElementalDamageSource eds2
			? eds2
			: new ElementalDamageSource(source, ElementalApplications.gaugeUnits((LivingEntity)(Entity) this, Element.PHYSICAL, 0), InternalCooldownContext.ofNone(source.getEntity()));

		sevenelements$subdamage += amount;

		if (sevenelements$subdamage < 1) return;

		final float extra = sevenelements$subdamage - (float) Math.floor(sevenelements$subdamage);

		sevenelements$subdamage = (float) Math.floor(sevenelements$subdamage);

		final AABB boundingBox = this.getBoundingBox();

		final double x = this.getX() + (boundingBox.getXsize() * 1.25 * Math.random());
		final double y = this.getY() + (boundingBox.getYsize() * 0.50 * Math.random()) + 0.50;
		final double z = this.getZ() + (boundingBox.getZsize() * 1.25 * Math.random());
		final Vec3 pos = new Vec3(x, y, z);
		final boolean isCrit = source.getEntity() instanceof final Player player
			&& ((IPlayerEntity) player).sevenelements$isCrit(eds);

		final Element element = eds.getElementalApplication().getElement();
		final ShowElementalDamageS2CPayload showElementalDMGPacket = new ShowElementalDamageS2CPayload(pos, element, sevenelements$subdamage, isCrit);

		sevenelements$subdamage = extra;

		for (final ServerPlayer player : PlayerLookup.tracking(this)) {
			if (player.getId() == this.getId()) return;

			ServerPlayNetworking.send(player, showElementalDMGPacket);
		}
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
	private void sevenelements$triggerDendroCoreReactions(final ServerLevel world, final DamageSource source) {
		if (!(source instanceof final ElementalDamageSource eds)) return;

		final Element element = eds.getElementalApplication().getElement();

		if (element != Element.PYRO && element != Element.ELECTRO) return;

		this.level()
			.getEntitiesOfClass(DendroCoreEntity.class, BoxUtil.multiplyBox(this.getBoundingBox(), 2), dc -> true)
			.forEach(dc -> dc.hurtServer(world, source, 1));
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
}
