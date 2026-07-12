package io.github.xrickastley.sevenelements.mixin;

import java.util.Optional;
import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;
import io.github.xrickastley.sevenelements.interfaces.InfusableProjectile;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin
	extends EntityMixin
	implements Ownable, InfusableProjectile
{
	@Unique
	private @Nullable ItemStack sevenelements$originStack;
	@Unique
	private @Nullable ItemStack sevenelements$projectileStack;

	@Inject(
		method = "writeCustomData",
		at = @At("TAIL")
	)
	public void writeInfusionToData(WriteView view, CallbackInfo ci) {
		view.putNullable("seven-elements:origin_stack", ItemStack.CODEC, this.sevenelements$originStack);
		view.putNullable("seven-elements:projectile_stack", ItemStack.CODEC, this.sevenelements$projectileStack);
	}

	@Inject(
		method = "readCustomData",
		at = @At("TAIL")
	)
	public void readStacksFromData(ReadView view, CallbackInfo ci) {
		this.sevenelements$originStack = view
			.read("seven-elements:origin_stack", ItemStack.CODEC)
			.orElse(this.sevenelements$originStack);

		this.sevenelements$projectileStack = view
			.read("seven-elements:projectile_stack", ItemStack.CODEC)
			.orElse(this.sevenelements$projectileStack);
	}

	@Override
	protected boolean sevenelements$modifyOnFire(boolean original) {
		return original
			|| (this.sevenelements$getElement() == Element.PYRO
				&& this.getEntityWorld() instanceof final ServerWorld world
				&& world.getGameRules().getBoolean(SevenElementsGameRules.PYRO_DOES_FIRE_EFFECTS)
			);
	}

	@Unique
	@Override
	public void sevenelements$setOriginStack(@Nullable ItemStack originStack) {
		this.sevenelements$originStack = ClassInstanceUtil.mapOrNull(originStack, ItemStack::copy);
	}

	@Unique
	@Override
	public void sevenelements$setProjectileStack(@Nullable ItemStack projectileStack) {
		this.sevenelements$projectileStack = ClassInstanceUtil.mapOrNull(projectileStack, ItemStack::copy);
	}

	@Unique
	@Override
	public Optional<ElementalDamageSource> sevenelements$attemptInfusion(DamageSource source, Entity target) {
		return Optional.ofNullable(
			JavaScriptUtil.nullishCoalesingFn(
				Functions.supplier(this.sevenelements$getInfusion(this.sevenelements$originStack), source, target),
				Functions.supplier(this.sevenelements$getInfusion(this.sevenelements$projectileStack), source, target)
			)
		);
	}

	@Unique
	private BiFunction<DamageSource, Entity, @Nullable ElementalDamageSource> sevenelements$getInfusion(ItemStack stack) {
		return (source, target) -> Optional.ofNullable(stack)
			.map(stack2 -> stack2.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT))
			.flatMap(Functions.withArgument(ElementalInfusionComponent::apply, source, target))
			.orElse(null);
	}

	@Unique
	private @Nullable Element sevenelements$getElement() {
		return JavaScriptUtil.nullishCoalesing(
			this.sevenelements$getElement(this.sevenelements$originStack),
			this.sevenelements$getElement(this.sevenelements$projectileStack)
		);
	}

	@Unique
	private @Nullable Element sevenelements$getElement(ItemStack stack) {
		return Optional.ofNullable(stack)
			.map(stack2 -> stack2.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT))
			.map(ElementalInfusionComponent::getElement)
			.orElse(null);
	}
}
