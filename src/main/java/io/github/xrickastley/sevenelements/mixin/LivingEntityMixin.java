package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.component.ElementComponent;
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
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
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

	@Unique
	private float sevenelements$subdamage;
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
		} else if (this.getBlockStateAtPos().getBlock() == Blocks.FIRE && this.getWorld().getGameRules().getBoolean(SevenElementsGameRules.PYRO_FROM_FIRE)) {
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
	private float applyCrystallizeShield(float amount, @Local(argsOnly = true) DamageSource source) {
		final ElementComponent component = ElementComponent.KEY.get(this);
		final float finalAmount = amount - component.reduceCrystallizeShield(source, amount);

		if (finalAmount < amount)
			this.getWorld().playSound(null, this.getBlockPos(), SevenElementsSoundEvents.CRYSTALLIZE_SHIELD_HIT, SoundCategory.PLAYERS, 1.0f, 1.0f);

		if (finalAmount <= 0) this.sevenelements$blockedByCrystallizeShield = true;

		return finalAmount;
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
	private boolean preventKnockbackIfCrystallize(boolean original, @Local(argsOnly = true) DamageSource source, @Share("sevenelements$hasCrystallizeShield") LocalBooleanRef hasCrystallizeShield) {
		final ElementComponent component = ElementComponent.KEY.get(this);

		return original || component.reducedCrystallizeShield();
	}

	@Inject(
		method = "applyDamage",
		at = @At("TAIL")
	)
	private void elementDamageHandler(final DamageSource source, float amount, CallbackInfo ci) {
		this.sevenelements$triggerDendroCoreReactions(source);

		if (!source.sevenelements$displayDamage()) return;

		final ElementalDamageSource eds = source instanceof final ElementalDamageSource eds2
			? eds2
			: new ElementalDamageSource(source, ElementalApplications.gaugeUnits((LivingEntity)(Entity) this, Element.PHYSICAL, 0), InternalCooldownContext.ofNone(source.getAttacker()));

		sevenelements$subdamage += amount;

		if (sevenelements$subdamage < 1) return;

		final float extra = sevenelements$subdamage - (float) Math.floor(sevenelements$subdamage);

		sevenelements$subdamage = (float) Math.floor(sevenelements$subdamage);

		final World world = this.getWorld();

		if (world.isClient || !(world instanceof ServerWorld)) return;

		final Box boundingBox = this.getBoundingBox();

		final double x = this.getX() + (boundingBox.getXLength() * 1.25 * Math.random());
		final double y = this.getY() + (boundingBox.getYLength() * 0.50 * Math.random()) + 0.50;
		final double z = this.getZ() + (boundingBox.getZLength() * 1.25 * Math.random());
		final Vec3d pos = new Vec3d(x, y, z);
		final boolean isCrit = eds.getOriginalSource() != null && source.getAttacker() instanceof final PlayerEntity player && ((IPlayerEntity) player).sevenelements$isCrit(eds.getOriginalSource());

		final Element element = eds.getElementalApplication().getElement();
		final ShowElementalDamageS2CPayload showElementalDMGPacket = new ShowElementalDamageS2CPayload(pos, element, sevenelements$subdamage, isCrit);

		sevenelements$subdamage = extra;

		for (final ServerPlayerEntity player : PlayerLookup.tracking(this)) {
			if (player.getId() == this.getId()) return;

			ServerPlayNetworking.send(player, showElementalDMGPacket);
		}
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

	@Unique
	private void sevenelements$triggerDendroCoreReactions(final DamageSource source) {
		if (!(source instanceof final ElementalDamageSource eds)) return;

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
}
