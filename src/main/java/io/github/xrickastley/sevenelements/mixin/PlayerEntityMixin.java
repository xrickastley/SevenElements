package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.interfaces.IPlayerEntity;
import io.github.xrickastley.sevenelements.networking.ShowElementalDamageS2CPayload;
import io.github.xrickastley.sevenelements.util.BoxUtil;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin
	extends LivingEntity
	implements IPlayerEntity
{
	public PlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(EntityType.PLAYER, world);
		throw new AssertionError();
	}

	@Unique
	private float sevenelements$subdamage;

	@Unique
	private List<DamageSource> sevenelements$critDamageSources = new ArrayList<>();

	@Unique
	@Override
	public boolean sevenelements$isCrit(DamageSource source) {
		return this.sevenelements$critDamageSources != null && this.sevenelements$critDamageSources.contains(source);
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
	private float applyCrystallizeShield(float amount, @Local(argsOnly = true) DamageSource source) {
		final ElementComponent component = ElementComponent.KEY.get(this);
		final float finalAmount = amount - component.reduceCrystallizeShield(source, amount);

		if (finalAmount < amount)
			this.getWorld().playSound(null, this.getBlockPos(), SevenElementsSoundEvents.CRYSTALLIZE_SHIELD_HIT, SoundCategory.PLAYERS, 1.0f, 1.0f);

		if (finalAmount <= 0) this.sevenelements$setBlockedByCrystallizeShield(true);

		return finalAmount;
	}

	// why are there two separate knockbacks :sob:
	@Definition(id = "k", local = @Local(type = float.class, ordinal = 5))
	@Expression("k > 0.0")
	@ModifyExpressionValue(
		method = "attack",
		at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private boolean preventKnockbackIfCrystallize(boolean original, @Local(argsOnly = true) Entity entity) {
		if (!(entity instanceof final LivingEntity livingEntity)) return original;

		final ElementComponent component = ElementComponent.KEY.get(livingEntity);

		return original && !component.reducedCrystallizeShield();
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
		if (sevenelements$critDamageSources == null) sevenelements$critDamageSources = new ArrayList<>();

		if (crit) sevenelements$critDamageSources.add(source);

		return source;
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
		if (sevenelements$critDamageSources == null) sevenelements$critDamageSources = new ArrayList<>();

		if (crit) sevenelements$critDamageSources.add(source);

		return source;
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void removeCritDS(CallbackInfo ci) {
		if (sevenelements$critDamageSources != null)
			sevenelements$critDamageSources.clear();
		else
			sevenelements$critDamageSources = new ArrayList<>();
	}

	@Inject(
		method = "applyDamage",
		at = @At("TAIL")
	)
	private void damageHandlers_elements(final DamageSource source, float amount, CallbackInfo ci) {
		this.sevenelements$triggerDendroCoreReactions(source);

		if (!source.sevenelements$displayDamage()) return;

		final ElementalDamageSource eds = source instanceof final ElementalDamageSource eds2
			? eds2
			: new ElementalDamageSource(source, ElementalApplications.gaugeUnits(this, Element.PHYSICAL, 0), InternalCooldownContext.ofNone(source.getAttacker()));

		sevenelements$subdamage += amount;

		if (sevenelements$subdamage < 1) return;

		final float extra = sevenelements$subdamage - (float) Math.floor(sevenelements$subdamage);

		sevenelements$subdamage = (float) Math.floor(sevenelements$subdamage);

		final World world = this.getWorld();

		if (world.isClient || !(world instanceof ServerWorld)) return;

		final Box boundingBox = this.getBoundingBox();

		final double x = this.getX() + (boundingBox.getLengthX() * 1.25 * Math.random());
		final double y = this.getY() + (boundingBox.getLengthY() * 0.50 * Math.random()) + 0.50;
		final double z = this.getZ() + (boundingBox.getLengthZ() * 1.25 * Math.random());
		final Vec3d pos = new Vec3d(x, y, z);
		final boolean isCrit = eds.getOriginalSource() != null
			&& source.getAttacker() instanceof final PlayerEntity player
			&& ((IPlayerEntity) player).sevenelements$isCrit(eds.getOriginalSource());

		final Element element = eds.getElementalApplication().getElement();
		final ShowElementalDamageS2CPayload showElementalDMGPacket = new ShowElementalDamageS2CPayload(pos, element, sevenelements$subdamage, isCrit);

		sevenelements$subdamage = extra;

		for (final ServerPlayerEntity player : PlayerLookup.tracking(this)) {
			if (player.getId() == this.getId()) return;

			ServerPlayNetworking.send(player, showElementalDMGPacket);
		}
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
}
