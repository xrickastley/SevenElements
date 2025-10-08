package io.github.xrickastley.sevenelements.mixin;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.interfaces.InfusableProjectile;
import io.github.xrickastley.sevenelements.util.ImmutablePair;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin
	extends Entity
	implements Ownable, InfusableProjectile
{
	@Unique
	private Pair<ElementalApplication.Builder, InternalCooldownContext.Builder> sevenelements$infusionComponent;

   	public ProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
   		super(entityType, world);

		throw new AssertionError();
	}

	@Unique
	@Override
	public void sevenelements$setOriginStack(@Nullable ItemStack originStack) {
		if (originStack == null) return;

		final ElementalInfusionComponent component = ElementalInfusionComponent.get(originStack);

		this.sevenelements$infusionComponent = new ImmutablePair<>(
			component.elementalInfusion(),
			component.internalCooldown()
		);
	}

	@Unique
	@Override
	public Optional<ElementalDamageSource> sevenelements$attemptInfusion(DamageSource source, Entity _target) {
		return this.sevenelements$infusionComponent == null || this.sevenelements$infusionComponent.getLeft() == null || this.sevenelements$infusionComponent.getRight() == null
			? Optional.empty()
			: _target instanceof final LivingEntity target && source.getAttacker() instanceof final LivingEntity attacker
				? Optional.of(
					new ElementalDamageSource(
						source,
						this.sevenelements$infusionComponent.getLeft().build(target),
						this.sevenelements$infusionComponent.getRight().build(attacker)
					)
				)
				: null;
	}

	@Inject(
		method = "writeCustomDataToNbt",
		at = @At("TAIL")
	)
	public void writeInfusionToNbt(NbtCompound nbt, CallbackInfo ci) {
		if (this.sevenelements$infusionComponent == null || this.sevenelements$infusionComponent.getLeft() == null || this.sevenelements$infusionComponent.getRight() == null) return;

		final NbtCompound componentNbt = new NbtCompound();

		componentNbt.put(
			"elemental_infusion",
			ElementalApplication.Builder.CODEC
				.encodeStart(NbtOps.INSTANCE, this.sevenelements$infusionComponent.getLeft())
				.resultOrPartial(SevenElements.sublogger()::error)
				.orElseThrow()
		);

		componentNbt.put(
			"internal_cooldown",
			InternalCooldownContext.Builder.CODEC
				.encodeStart(NbtOps.INSTANCE, this.sevenelements$infusionComponent.getRight())
				.resultOrPartial(SevenElements.sublogger()::error)
				.orElseThrow()
		);

		nbt.put("seven-elements:elemental_infusion", componentNbt);
	}

	@Inject(
		method = "readCustomDataFromNbt",
		at = @At("TAIL")
	)
	public void readInfusionFromNbt(NbtCompound nbt, CallbackInfo ci) {
		if (!nbt.contains("seven-elements:elemental_infusion")) return;

		final NbtCompound componentNbt = (NbtCompound) nbt.get("seven-elements:elemental_infusion");

		this.sevenelements$infusionComponent = new ImmutablePair<ElementalApplication.Builder,InternalCooldownContext.Builder>(
			ElementalApplication.Builder.CODEC
				.parse(NbtOps.INSTANCE, componentNbt.get("elemental_infusion"))
				.resultOrPartial(SevenElements.sublogger()::error)
				.orElseThrow(),
			InternalCooldownContext.Builder.CODEC
				.parse(NbtOps.INSTANCE, componentNbt.get("internal_cooldown"))
				.resultOrPartial(SevenElements.sublogger()::error)
				.orElseThrow()
		);
	}
}
