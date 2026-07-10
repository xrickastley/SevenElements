package io.github.xrickastley.sevenelements.mixin;

import java.util.Optional;
import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.SevenElements;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.world.ServerWorld;

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
		method = "writeCustomDataToNbt",
		at = @At("TAIL")
	)
	public void writeStacksToNbt(NbtCompound nbt, CallbackInfo ci) {
		final RegistryOps<NbtElement> registryOps = this.getWorld().getRegistryManager().getOps(NbtOps.INSTANCE);

		if (this.sevenelements$originStack != null) {
			final NbtElement componentNbt = ItemStack.CODEC
				.encodeStart(registryOps, this.sevenelements$originStack)
				.resultOrPartial(SevenElements.sublogger()::error)
				.orElseThrow();

			nbt.put("seven-elements:origin_stack", componentNbt);
		}

		if (this.sevenelements$projectileStack != null) {
			final NbtElement componentNbt = ItemStack.CODEC
				.encodeStart(registryOps, this.sevenelements$projectileStack)
				.resultOrPartial(SevenElements.sublogger()::error)
				.orElseThrow();

			nbt.put("seven-elements:projectile_stack", componentNbt);
		}
	}

	@Inject(
		method = "readCustomDataFromNbt",
		at = @At("TAIL")
	)
	public void readStacksFromNbt(NbtCompound nbt, CallbackInfo ci) {
		final RegistryOps<NbtElement> registryOps = this.getWorld().getRegistryManager().getOps(NbtOps.INSTANCE);

		if (nbt.contains("seven-elements:origin_stack")) {
			this.sevenelements$originStack = ItemStack.CODEC
				.parse(registryOps, nbt.get("seven-elements:origin_stack"))
				.resultOrPartial(SevenElements.sublogger()::error)
				.orElseThrow();
		}

		if (nbt.contains("seven-elements:projectile_stack")) {
			this.sevenelements$projectileStack = ItemStack.CODEC
				.parse(registryOps, nbt.get("seven-elements:projectile_stack"))
				.resultOrPartial(SevenElements.sublogger()::error)
				.orElseThrow();
		}
	}

	@Override
	protected boolean sevenelements$modifyOnFire(boolean original) {
		return original
			|| (this.sevenelements$getElement() == Element.PYRO
				&& this.getWorld() instanceof final ServerWorld world
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
