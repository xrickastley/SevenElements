package io.github.xrickastley.sevenelements.mixin;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.interfaces.InfusableProjectile;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@Mixin(Projectile.class)
public abstract class ProjectileMixin
	extends Entity
	implements TraceableEntity, InfusableProjectile
{
	@Unique
	private ElementalInfusionComponent sevenelements$infusionComponent;

	public ProjectileMixin(EntityType<? extends Projectile> entityType, Level world) {
		super(entityType, world);

		throw new AssertionError();
	}

	@Unique
	@Override
	public void sevenelements$setOriginStack(@Nullable ItemStack originStack) {
		if (originStack == null) return;

		this.sevenelements$infusionComponent = originStack.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);
	}

	@Unique
	@Override
	public Optional<ElementalDamageSource> sevenelements$attemptInfusion(DamageSource source, Entity target) {
		return this.sevenelements$infusionComponent == null
			? Optional.empty()
			: this.sevenelements$infusionComponent.apply(source, target);
	}

	@Inject(
		method = "addAdditionalSaveData",
		at = @At("TAIL")
	)
	public void writeInfusionToNbt(ValueOutput view, CallbackInfo ci) {
		if (this.sevenelements$infusionComponent == null) return;

		view.store("seven-elements:elemental_infusion", ElementalInfusionComponent.CODEC, this.sevenelements$infusionComponent);
	}

	@Inject(
		method = "readAdditionalSaveData",
		at = @At("TAIL")
	)
	public void readInfusionFromNbt(ValueInput view, CallbackInfo ci) {
		this.sevenelements$infusionComponent = view.read("seven-elements:elemental_infusion", ElementalInfusionComponent.CODEC)
				.orElse(this.sevenelements$infusionComponent);
	}
}
