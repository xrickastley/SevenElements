package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.entity.CrystallizeShardEntity;
import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;
import io.github.xrickastley.sevenelements.interfaces.IEntity;
import io.github.xrickastley.sevenelements.item.ElementalAttunementSmithingTemplateItem;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntity {
	@Shadow
	public abstract World getWorld();

	@Shadow
	public abstract Vec3d getPos();

	@Shadow
	public abstract float getHeight();

	@Final
	@Inject(
		method = "onStruckByLightning",
		at = @At("TAIL")
	)
	private void applyElectroOnLightning(ServerWorld world, LightningEntity lightning, CallbackInfo ci) {
		final PlayerEntity player = ClassInstanceUtil.castOrNull(this, PlayerEntity.class);

		ElementalAttunementSmithingTemplateItem.ELECTRO_ATTUNEMENT_PITY.roll(player);
	}

	@Final
	@ModifyArg(
		method = "onStruckByLightning",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z"
		)
	)
	private DamageSource applyElectroOnLightning(DamageSource source, @Local(argsOnly = true) ServerWorld world) {
		return (Entity)(Object) this instanceof final LivingEntity entity && world.getGameRules().getBoolean(SevenElementsGameRules.ELECTRO_FROM_LIGHTNING)
			? new ElementalDamageSource(source, ElementalApplications.gaugeUnits(entity, Element.ELECTRO, 2.0), InternalCooldownContext.ofType(null, "seven-elements:natural_environment", InternalCooldownType.INTERVAL_ONLY).forced())
			: source;
	}

	@Inject(
		method = "setPos",
		at = @At("TAIL")
	)
	private void syncOnPosChangeIfCrystallizeShard(double x, double y, double z, CallbackInfo ci) {
		final CrystallizeShardEntity crystallizeShard = ClassInstanceUtil.castOrNull(this, CrystallizeShardEntity.class);

		if (crystallizeShard == null) return;

		// Sync after pos change, that way PlayerTracking.lookup properly works.
		crystallizeShard.syncToPlayers();
	}

	@ModifyReturnValue(
		method = "isOnFire",
		at = @At("RETURN")
	)
	protected boolean sevenelements$modifyOnFire(boolean original) {
		return original;
	}

	@Unique
	@Override
	public boolean sevenelements$isFullySubmergedIn(TagKey<Fluid> fluidTag) {
		final World world = this.getWorld();
		final Vec3d pos = this.getPos();
		final double start = pos.getY();
		final double end = start + this.getHeight();

		return Stream.iterate(start, y -> y < Math.ceil(end), y -> y + 1)
			.allMatch(y -> {
				final BlockPos blockPos = BlockPos.ofFloored(pos.x, y, pos.z);
				final FluidState fluidState = world.getFluidState(blockPos);

				return fluidState.getHeight(world, blockPos) >= Math.min(1, end - y)
					&& fluidState.isIn(fluidTag);
			});
	}
}
