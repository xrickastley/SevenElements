package io.github.xrickastley.sevenelements.component;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;

public final class ElementalInfusionComponent extends ItemComponent {
	private static final Logger LOGGER = SevenElements.sublogger();

	public static final ComponentKey<ElementalInfusionComponent> KEY = ComponentRegistry.getOrCreate(SevenElements.identifier("elemental_infusion"), ElementalInfusionComponent.class);

	public ElementalInfusionComponent(ItemStack stack) {
		super(stack);
	}

	public static Optional<ElementalDamageSource> applyToDamageSource(DamageSource source, Entity target) {
		try {
			if (source.isIndirect() || !(target instanceof final LivingEntity livingTarget) || !(source.getAttacker() instanceof final LivingEntity attacker)) return Optional.empty();

			final ElementalInfusionComponent component = ElementalInfusionComponent.KEY.get(attacker.getMainHandStack());

			if (component == null || !component.hasElementalInfusion()) return Optional.empty();

			return Optional.of(
				new ElementalDamageSource(
					source,
					component.getElementalInfusion(livingTarget),
					component.internalCooldown().build(attacker)
				)
			);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static void applyInfusion(ItemStack stack, ElementalApplication.Builder applicationBuilder, InternalCooldownContext.Builder icdBuilder) {
		final ElementalInfusionComponent component = ElementalInfusionComponent.get(stack);

		if (component == null) return;

		component.setElementalInfusion(applicationBuilder);
		component.setInternalCooldown(icdBuilder);

		return;
	}

	public static boolean removeInfusion(ItemStack stack) {
		if (!ElementalInfusionComponent.hasInfusion(stack)) return false;

		final ElementalInfusionComponent component = ElementalInfusionComponent.get(stack);

		component.remove("elemental_infusion");
		component.remove("internal_cooldown");

		return true;
	}

	public static boolean hasInfusion(ItemStack stack) {
		return ElementalInfusionComponent
			.get(stack)
			.hasElementalInfusion();
	}

	public static ElementalInfusionComponent get(ItemStack stack) {
		return ElementalInfusionComponent.KEY.get(stack);
	}

	public @Nullable ElementalApplication.Builder elementalInfusion() {
		return this.hasElementalInfusion()
			? ElementalApplication.Builder.CODEC
				.parse(NbtOps.INSTANCE, this.getCompound("elemental_infusion"))
				.resultOrPartial(LOGGER::error)
				.orElseThrow()
			: null;
	}

	public @Nullable InternalCooldownContext.Builder internalCooldown() {
		return this.hasTag("internal_cooldown", NbtElement.COMPOUND_TYPE)
			? InternalCooldownContext.Builder.CODEC
				.parse(NbtOps.INSTANCE, this.getCompound("internal_cooldown"))
				.resultOrPartial(LOGGER::error)
				.orElseThrow()
			: null;
	}

	public boolean hasElementalInfusion() {
		return this.hasTag("elemental_infusion", NbtElement.COMPOUND_TYPE);
	}

	public @Nullable ElementalApplication getElementalInfusion(LivingEntity target) {
		return this.hasElementalInfusion()
			? this.elementalInfusion().build(target)
			: null;
	}

	public @Nullable Element getElement() {
		return this.hasElementalInfusion()
			? this.elementalInfusion().getElement()
			: null;
	}

	public double getGaugeUnits() {
		return this.hasElementalInfusion()
			? this.getGaugeUnits()
			: 0;
	}

	public void setElementalInfusion(ElementalApplication.Builder builder) {
		this.putCompound(
			"elemental_infusion",
			(NbtCompound) ElementalApplication.Builder.CODEC
				.encodeStart(NbtOps.INSTANCE, builder)
				.resultOrPartial(LOGGER::error)
				.orElseThrow()
		);
	}

	public void setInternalCooldown(InternalCooldownContext.Builder builder) {
		this.putCompound(
			"internal_cooldown",
			(NbtCompound) InternalCooldownContext.Builder.CODEC
				.encodeStart(NbtOps.INSTANCE, builder)
				.resultOrPartial(LOGGER::error)
				.orElseThrow()
		);
	}

	public Optional<ElementalDamageSource> apply(DamageSource source, Entity target) {
		if (!(target instanceof final LivingEntity livingTarget) || !(source.getAttacker() instanceof final LivingEntity attacker) || !this.hasElementalInfusion())
			return Optional.empty();

		return Optional.of(
			new ElementalDamageSource(
				source,
				this.getElementalInfusion(livingTarget),
				this.internalCooldown().build(attacker)
			)
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (!(obj instanceof final ElementalInfusionComponent component)) return false;

		return Objects.equals(this.elementalInfusion(), component.elementalInfusion())
			&& Objects.equals(this.internalCooldown(), component.internalCooldown());
	}
}
