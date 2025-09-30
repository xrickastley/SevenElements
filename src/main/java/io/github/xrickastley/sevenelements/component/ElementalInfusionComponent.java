package io.github.xrickastley.sevenelements.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;

public record ElementalInfusionComponent(@Nullable ElementalApplication.Builder elementalInfusion, @Nullable InternalCooldownContext.Builder internalCooldown) {
	public static final Codec<ElementalInfusionComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ElementalApplication.Builder.CODEC.fieldOf("elemental_infusion").forGetter(ElementalInfusionComponent::elementalInfusion),
		InternalCooldownContext.Builder.CODEC.optionalFieldOf("internal_cooldown", InternalCooldownContext.Builder.ofNone()).forGetter(ElementalInfusionComponent::internalCooldown)
	).apply(instance, ElementalInfusionComponent::new));

	public static Optional<ElementalDamageSource> applyToDamageSource(DamageSource source, Entity target) {
		try {
			if (!source.isDirect() || !(target instanceof final LivingEntity livingTarget) || !(source.getAttacker() instanceof final LivingEntity attacker)) return Optional.empty();

			final @Nullable ElementalInfusionComponent component = attacker.getWeaponStack().get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);

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
		stack.set(
			SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT,
			new ElementalInfusionComponent(applicationBuilder, icdBuilder)
		);
	}

	public static boolean removeInfusion(ItemStack stack) {
		if (!ElementalInfusionComponent.hasInfusion(stack)) return false;

		stack.remove(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);

		return true;
	}

	public static boolean hasInfusion(ItemStack stack) {
		return JavaScriptUtil.isTruthy(
			ClassInstanceUtil.mapOrNull(
				stack.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT),
				ElementalInfusionComponent::hasElementalInfusion
			)
		);
	}

	public static ElementalInfusionComponent ofNone() {
		return new ElementalInfusionComponent(null, null);
	}

	public boolean hasElementalInfusion() {
		return this.elementalInfusion != null;
	}

	public @Nullable ElementalApplication getElementalInfusion(LivingEntity target) {
		return this.hasElementalInfusion()
			? elementalInfusion.build(target)
			: null;
	}

	public @Nullable Element getElement() {
		return this.hasElementalInfusion()
			? elementalInfusion.getElement()
			: null;
	}

	public double getGaugeUnits() {
		return this.hasElementalInfusion()
			? elementalInfusion.getGaugeUnits()
			: 0;
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

		return Objects.equals(this.elementalInfusion, component.elementalInfusion)
			&& Objects.equals(this.internalCooldown, component.internalCooldown);
	}
}
