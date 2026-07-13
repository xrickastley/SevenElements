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

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntity {
	@Shadow
	public abstract Level level();

	@Shadow
	public abstract Vec3 position();

	@Shadow
	public abstract float getBbHeight();

	@Final
	@Inject(
		method = "thunderHit",
		at = @At("TAIL")
	)
	private void applyElectroOnLightning(ServerLevel world, LightningBolt lightning, CallbackInfo ci) {
		final Player player = ClassInstanceUtil.castOrNull(this, Player.class);

		ElementalAttunementSmithingTemplateItem.ELECTRO_ATTUNEMENT_PITY.roll(player);
	}

	@Final
	@ModifyArg(
		method = "thunderHit",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
		)
	)
	private DamageSource applyElectroOnLightning(DamageSource source, @Local(argsOnly = true) ServerLevel world) {
		return (Entity)(Object) this instanceof final LivingEntity entity
			? new ElementalDamageSource(
				source,
				ElementalApplications
					.gaugeUnits(entity, Element.ELECTRO, world.getGameRules().get(SevenElementsGameRules.ELECTRO_FROM_LIGHTNING) ? 2.0 : 0),
				InternalCooldownContext.ofType(null, "seven-elements:natural_environment", InternalCooldownType.INTERVAL_ONLY).forced()
			)
			: source;
	}

	@Inject(
		method = "setPosRaw",
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
		final Level world = this.level();
		final Vec3 pos = this.position();
		final double start = pos.y();
		final double end = start + this.getBbHeight();

		return Stream.iterate(start, y -> y < Math.ceil(end), y -> y + 1)
			.allMatch(y -> {
				final BlockPos blockPos = BlockPos.containing(pos.x, y, pos.z);
				final FluidState fluidState = world.getFluidState(blockPos);

				return fluidState.getHeight(world, blockPos) >= Math.min(1, end - y)
					&& fluidState.is(fluidTag);
			});
	}
}
