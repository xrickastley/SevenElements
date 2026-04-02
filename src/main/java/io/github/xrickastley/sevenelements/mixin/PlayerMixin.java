package io.github.xrickastley.sevenelements.mixin;

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

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.interfaces.DamageSourceWrapper;
import io.github.xrickastley.sevenelements.interfaces.IPlayerEntity;
import io.github.xrickastley.sevenelements.networking.ShowElementalDamageS2CPayload;
import io.github.xrickastley.sevenelements.util.BoxUtil;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(Player.class)
public abstract class PlayerMixin
	extends LivingEntity
	implements IPlayerEntity
{
	public PlayerMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(EntityType.PLAYER, world);
		throw new AssertionError();
	}

	@Unique
	private float sevenelements$subdamage;

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
		method = "actuallyHurt",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"
		),
		ordinal = 0,
		argsOnly = true
	)
	private float applyCrystallizeShield(float amount, @Local(argsOnly = true) DamageSource source) {
		final ElementComponent component = ElementComponent.KEY.get(this);
		final float finalAmount = amount - component.reduceCrystallizeShield(source, amount);

		if (finalAmount < amount)
			this.level().playSound(null, this.blockPosition(), SevenElementsSoundEvents.CRYSTALLIZE_SHIELD_HIT, SoundSource.PLAYERS, 1.0f, 1.0f);

		if (finalAmount <= 0) this.sevenelements$setBlockedByCrystallizeShield(true);

		return finalAmount;
	}

	// why are there two separate knockbacks :sob:
	@ModifyExpressionValue(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;isSprinting()Z"
		)
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
			target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
		),
		index = 0
	)
	private DamageSource checkForCritMain(DamageSource source, @Local(ordinal = 2) boolean crit) {
		if (sevenelements$critDamageSources == null) sevenelements$critDamageSources = new HashSet<>();

		if (crit) sevenelements$critDamageSources.add(source);

		return source;
	}

	@ModifyArg(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;F)V"
		),
		index = 2
	)
	private DamageSource checkForCritSweep(DamageSource source, @Local(ordinal = 2) boolean crit) {
		if (sevenelements$critDamageSources == null) sevenelements$critDamageSources = new HashSet<>();

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
			sevenelements$critDamageSources = new HashSet<>();
	}

	@Inject(
		method = "actuallyHurt",
		at = @At("TAIL")
	)
	private void elementDamageHandler(ServerLevel world, DamageSource source, float amount, CallbackInfo ci) {
		this.sevenelements$triggerDendroCoreReactions(world, source);

		if (!source.sevenelements$displayDamage()) return;

		final ElementalDamageSource eds = source instanceof final ElementalDamageSource eds2
			? eds2
			: new ElementalDamageSource(source, ElementalApplications.gaugeUnits(this, Element.PHYSICAL, 0), InternalCooldownContext.ofNone(source.getEntity()));

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

	@Unique
	private void sevenelements$triggerDendroCoreReactions(final ServerLevel world, final DamageSource source) {
		if (!(source instanceof final ElementalDamageSource eds)) return;

		final Element element = eds.getElementalApplication().getElement();

		if (element != Element.PYRO && element != Element.ELECTRO) return;

		this.level()
			.getEntitiesOfClass(DendroCoreEntity.class, BoxUtil.multiplyBox(this.getBoundingBox(), 2), dc -> true)
			.forEach(dc -> dc.hurtServer(world, source, 1));
	}
}
