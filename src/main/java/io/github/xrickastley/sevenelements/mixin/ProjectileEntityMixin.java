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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin
	extends Entity
	implements Ownable, InfusableProjectile
{
	@Unique
	private ElementalInfusionComponent sevenelements$infusionComponent;

   	public ProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
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
		method = "writeCustomData",
		at = @At("TAIL")
	)
	public void writeInfusionToNbt(WriteView view, CallbackInfo ci) {
		if (this.sevenelements$infusionComponent == null) return;

		view.put("seven-elements:elemental_infusion", ElementalInfusionComponent.CODEC, this.sevenelements$infusionComponent);
	}

	@Inject(
		method = "readCustomData",
		at = @At("TAIL")
	)
	public void readInfusionFromNbt(ReadView view, CallbackInfo ci) {
		this.sevenelements$infusionComponent = view.read("seven-elements:elemental_infusion", ElementalInfusionComponent.CODEC)
				.orElse(this.sevenelements$infusionComponent);
	}
}
