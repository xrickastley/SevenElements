package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.interfaces.ILivingEntity;
import io.github.xrickastley.sevenelements.interfaces.IPlayerEntity;
import io.github.xrickastley.sevenelements.networking.ShowElementalDamageS2CPayload;
import io.github.xrickastley.sevenelements.util.BoxUtil;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin({ LivingEntity.class, Player.class })
public abstract class LivingEntityAndPlayerMixin
	extends Entity
	implements ILivingEntity
{
	public LivingEntityAndPlayerMixin(EntityType<?> type, Level world) {
		super(type, world);
		throw new AssertionError();
	}

	@Unique
	private float sevenelements$subdamage;

	@Inject(
		method = "actuallyHurt",
		at = @At("TAIL")
	)
	private void triggerDendroCoreReactions(final ServerLevel world, final DamageSource source, float amount, CallbackInfo ci) {
		if (!(source instanceof final ElementalDamageSource eds)) return;

		final Element element = eds.getElementalApplication().getElement();

		if (element != Element.PYRO && element != Element.ELECTRO) return;

		this.level()
			.getEntitiesOfClass(DendroCoreEntity.class, BoxUtil.multiplyBox(this.getBoundingBox(), 2), dc -> true)
			.forEach(dc -> dc.hurtServer(world, source, 1));
	}

	@Inject(
		method = "actuallyHurt",
		at = @At("TAIL")
	)
	private void elementDamageHandler(ServerLevel world, DamageSource source, float amount, CallbackInfo ci) {
		if (this.level().isClientSide() || !source.sevenelements$displayDamage() || (this.sevenelements$subdamage += amount) < 1) return;

		final ElementalDamageSource eds = source instanceof final ElementalDamageSource eds2
			? eds2
			: new ElementalDamageSource(source, ElementalApplications.gaugeUnits(ClassInstanceUtil.cast(this), Element.PHYSICAL, 0), InternalCooldownContext.ofNone(source.getEntity()));

		final float wholeDamage = (float) Math.floor(this.sevenelements$subdamage);

		this.sevenelements$subdamage -= wholeDamage;

		final AABB boundingBox = this.getBoundingBox();

		final double x = this.getX() + (boundingBox.getXsize() * Math.random());
		final double y = this.getY() + (boundingBox.getYsize() * (0.25 + (Math.random() / 2.0)));
		final double z = this.getZ() + (boundingBox.getZsize() * Math.random());
		final Vec3 pos = new Vec3(x, y, z);
		final boolean isCrit = source.getEntity() instanceof final Player player
			&& ((IPlayerEntity) player).sevenelements$isCrit(eds);

		final Element element = eds.getElementalApplication().getElement();
		final ShowElementalDamageS2CPayload showElementalDMGPacket = new ShowElementalDamageS2CPayload(pos, element, wholeDamage, isCrit);

		for (final ServerPlayer player : PlayerLookup.tracking(this)) {
			if (player.getId() == this.getId()) continue;

			ServerPlayNetworking.send(player, showElementalDMGPacket);
		}
	}
}
