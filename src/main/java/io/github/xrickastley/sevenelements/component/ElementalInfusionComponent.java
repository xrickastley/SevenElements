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

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;

public record ElementalInfusionComponent(@Nullable ElementalApplication.Builder elementalInfusion, @Nullable InternalCooldownContext.Builder internalCooldown) implements TooltipProvider {
	private static final List<Element> ELEMENTS = List.of(Element.PYRO, Element.HYDRO, Element.ANEMO, Element.ELECTRO, Element.DENDRO, Element.CRYO, Element.GEO);
	private static final List<Double> GAUGE_UNITS = List.of(1.0, 1.5, 2.0);

	public static final Codec<ElementalInfusionComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ElementalApplication.Builder.CODEC.fieldOf("elemental_infusion").forGetter(ElementalInfusionComponent::elementalInfusion),
		InternalCooldownContext.Builder.CODEC.optionalFieldOf("internal_cooldown", InternalCooldownContext.Builder.ofNone()).forGetter(ElementalInfusionComponent::internalCooldown)
	).apply(instance, ElementalInfusionComponent::new));

	@ApiStatus.Internal
	public static Tuple<Element, Double> generateAndApplyInfusion(ItemStack stack, Level world) {
		final DataComponentMap components = stack.getComponents();

		final Element element = components.has(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT)
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

		return new Tuple<>(element, gaugeUnits);
	}

	public static Optional<ElementalDamageSource> applyToDamageSource(DamageSource source, Entity target) {
		try {
			if (!source.isDirect() || !(target instanceof final LivingEntity livingTarget) || !(source.getEntity() instanceof final LivingEntity attacker)) return Optional.empty();

			final @Nullable ElementalInfusionComponent component = attacker.getWeaponItem().get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);

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
			stack.has(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT)
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
		if (!(target instanceof final LivingEntity livingTarget) || !(source.getEntity() instanceof final LivingEntity attacker) || !this.hasElementalInfusion())
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
	public void addToTooltip(Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
		final Builder icdContext = this.internalCooldown();

		textConsumer.accept(
			Component.empty()
				.append(Component.translatable("item.seven-elements.components.infusion.infusion").withStyle(ChatFormatting.WHITE))
				.append(ElementalApplication.Builder.getText(this.elementalInfusion()))
		);

		@Nullable InternalCooldownTag tag = ClassInstanceUtil.mapOrNull(icdContext, Builder::getTag);

		final Component tagText = tag != null
			? tag.getText(ChatFormatting.DARK_GRAY)
			: Component.literal("none").withStyle(ChatFormatting.RED);

		textConsumer.accept(
			Component.empty()
				.append(Component.translatable("item.seven-elements.components.infusion.tag").withStyle(ChatFormatting.WHITE))
				.append(tagText)
		);

		final InternalCooldownType icdType = JavaScriptUtil.nullishCoalesing(
			ClassInstanceUtil.mapOrNull(icdContext, Builder::getType),
			InternalCooldownType.DEFAULT
		);

		textConsumer.accept(
			Component.empty()
				.append(Component.translatable("item.seven-elements.components.infusion.type").withStyle(ChatFormatting.WHITE))
				.append(icdType.getText(true).withStyle(ChatFormatting.DARK_GRAY))
		);
	}
}
