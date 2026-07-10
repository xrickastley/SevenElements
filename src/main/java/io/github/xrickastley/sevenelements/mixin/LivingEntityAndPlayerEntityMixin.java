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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin({ LivingEntity.class, PlayerEntity.class })
public abstract class LivingEntityAndPlayerEntityMixin
	extends Entity
	implements ILivingEntity
{
	public LivingEntityAndPlayerEntityMixin(EntityType<?> type, World world) {
		super(type, world);
		throw new AssertionError();
	}

	@Unique
	private float sevenelements$subdamage;

	@Inject(
		method = "applyDamage",
		at = @At("TAIL")
	)
	private void triggerDendroCoreReactions(final ServerWorld world, final DamageSource source, float amount, CallbackInfo ci) {
		if (!(source instanceof final ElementalDamageSource eds)) return;

		final Element element = eds.getElementalApplication().getElement();

		if (element != Element.PYRO && element != Element.ELECTRO) return;

		this.getWorld()
			.getEntitiesByClass(DendroCoreEntity.class, BoxUtil.multiplyBox(this.getBoundingBox(), 2), dc -> true)
			.forEach(dc -> dc.damage(world, source, 1));
	}

	@Inject(
		method = "applyDamage",
		at = @At("TAIL")
	)
	private void elementDamageHandler(ServerWorld world, DamageSource source, float amount, CallbackInfo ci) {
		if (this.getWorld().isClient || !source.sevenelements$displayDamage() || (this.sevenelements$subdamage += amount) < 1) return;

		final ElementalDamageSource eds = source instanceof final ElementalDamageSource eds2
			? eds2
			: new ElementalDamageSource(source, ElementalApplications.gaugeUnits(ClassInstanceUtil.cast(this), Element.PHYSICAL, 0), InternalCooldownContext.ofNone(source.getAttacker()));

		final float wholeDamage = (float) Math.floor(this.sevenelements$subdamage);

		this.sevenelements$subdamage -= wholeDamage;

		final Box boundingBox = this.getBoundingBox();

		final double x = this.getX() + (boundingBox.getLengthX() * Math.random());
		final double y = this.getY() + (boundingBox.getLengthY() * (0.25 + (Math.random() / 2.0)));
		final double z = this.getZ() + (boundingBox.getLengthZ() * Math.random());
		final Vec3d pos = new Vec3d(x, y, z);
		final boolean isCrit = source.getAttacker() instanceof final PlayerEntity player
			&& ((IPlayerEntity) player).sevenelements$isCrit(eds);

		final Element element = eds.getElementalApplication().getElement();
		final ShowElementalDamageS2CPayload showElementalDMGPacket = new ShowElementalDamageS2CPayload(pos, element, wholeDamage, isCrit);

		for (final ServerPlayerEntity player : PlayerLookup.tracking(this)) {
			if (player.getId() == this.getId()) continue;

			ServerPlayNetworking.send(player, showElementalDMGPacket);
		}
	}
}
