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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@Mixin(Projectile.class)
public abstract class ProjectileMixin
	extends EntityMixin
	implements TraceableEntity, InfusableProjectile
{
	@Unique
	private @Nullable ItemStack sevenelements$originStack;
	@Unique
	private @Nullable ItemStack sevenelements$projectileStack;

	@Inject(
		method = "addAdditionalSaveData",
		at = @At("TAIL")
	)
	public void writeInfusionToData(ValueOutput view, CallbackInfo ci) {
		view.storeNullable("seven-elements:origin_stack", ItemStack.CODEC, this.sevenelements$originStack);
		view.storeNullable("seven-elements:projectile_stack", ItemStack.CODEC, this.sevenelements$projectileStack);
	}

	@Inject(
		method = "readAdditionalSaveData",
		at = @At("TAIL")
	)
	public void readStacksFromData(ValueInput view, CallbackInfo ci) {
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
				&& this.level() instanceof final ServerLevel world
				&& world.getGameRules().get(SevenElementsGameRules.PYRO_DOES_FIRE_EFFECTS)
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
