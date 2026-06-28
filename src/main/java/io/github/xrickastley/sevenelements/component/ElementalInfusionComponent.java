package io.github.xrickastley.sevenelements.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication.Type;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext.Builder;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.InternalCooldownTag;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;

import net.minecraft.component.ComponentMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public record ElementalInfusionComponent(@Nullable ElementalApplication.Builder elementalInfusion, @Nullable InternalCooldownContext.Builder internalCooldown) implements TooltipAppender {
	private static final List<Element> ELEMENTS = List.of(Element.PYRO, Element.HYDRO, Element.ANEMO, Element.ELECTRO, Element.DENDRO, Element.CRYO, Element.GEO);
	private static final List<Double> GAUGE_UNITS = List.of(1.0, 1.5, 2.0);

	public static final Codec<ElementalInfusionComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ElementalApplication.Builder.CODEC.fieldOf("elemental_infusion").forGetter(ElementalInfusionComponent::elementalInfusion),
		InternalCooldownContext.Builder.CODEC.optionalFieldOf("internal_cooldown", InternalCooldownContext.Builder.ofNone()).forGetter(ElementalInfusionComponent::internalCooldown)
	).apply(instance, ElementalInfusionComponent::new));

	@ApiStatus.Internal
	public static Pair<Element, Double> generateAndApplyInfusion(ItemStack stack, World world) {
		final ComponentMap components = stack.getComponents();

		final Element element = components.contains(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT)
			? components.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT).element()
			: ELEMENTS.get(world.getRandom().nextInt(ELEMENTS.size()));
		final double gaugeUnits = GAUGE_UNITS.get(world.getRandom().nextInt(GAUGE_UNITS.size()));

		ElementalInfusionComponent.applyInfusion(
			stack,
			ElementalApplications.builder()
				.setType(Type.GAUGE_UNIT)
				.setElement(element)
				.setGaugeUnits(gaugeUnits),
			InternalCooldownContext.builder()
				.setTag(InternalCooldownTag.of("seven-elements:elemental_infusion"))
				.setType(InternalCooldownType.DEFAULT)
		);

		return new Pair<>(element, gaugeUnits);
	}

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

	public static boolean applyInfusion(ItemStack stack, ElementalApplication.Builder applicationBuilder, InternalCooldownContext.Builder icdBuilder) {
		if (
			stack.contains(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT)
			&& stack.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT).element() != applicationBuilder.getElement()
		) return false;

		stack.set(
			SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT,
			new ElementalInfusionComponent(applicationBuilder, icdBuilder)
		);

		return true;
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

	@Override
	public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
		final Builder icdContext = this.internalCooldown();

		tooltip.accept(
			Text.empty()
				.append(Text.translatable("item.seven-elements.components.infusion.infusion").formatted(Formatting.WHITE))
				.append(ElementalApplication.Builder.getText(this.elementalInfusion()))
		);

		@Nullable InternalCooldownTag tag = ClassInstanceUtil.mapOrNull(icdContext, Builder::getTag);

		final Text tagText = tag != null
			? tag.getText(Formatting.DARK_GRAY)
			: Text.literal("none").formatted(Formatting.RED);

		tooltip.accept(
			Text.empty()
				.append(Text.translatable("item.seven-elements.components.infusion.tag").formatted(Formatting.WHITE))
				.append(tagText)
		);

		final InternalCooldownType icdType = JavaScriptUtil.nullishCoalesing(
			ClassInstanceUtil.mapOrNull(icdContext, Builder::getType),
			InternalCooldownType.DEFAULT
		);

		tooltip.accept(
			Text.empty()
				.append(Text.translatable("item.seven-elements.components.infusion.type").formatted(Formatting.WHITE))
				.append(icdType.getText(true).formatted(Formatting.DARK_GRAY))
		);
	}
}
