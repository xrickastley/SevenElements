package io.github.xrickastley.sevenelements.component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.interfaces.TooltipProvider;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication.Type;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext.Builder;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.InternalCooldownTag;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;

public final class ElementalInfusionComponent
	extends ItemComponent
	implements TooltipProvider
{
	private static final Logger LOGGER = SevenElements.sublogger();
	private static final List<Element> ELEMENTS = List.of(Element.PYRO, Element.HYDRO, Element.ANEMO, Element.ELECTRO, Element.DENDRO, Element.CRYO, Element.GEO);
	private static final List<Double> GAUGE_UNITS = List.of(1.0, 1.5, 2.0);

	public static final ComponentKey<ElementalInfusionComponent> KEY = ComponentRegistry.getOrCreate(SevenElements.identifier("elemental_infusion"), ElementalInfusionComponent.class);

	public ElementalInfusionComponent(ItemStack stack) {
		super(stack);
	}

	@ApiStatus.Internal
	public static Pair<Element, Double> generateAndApplyInfusion(ItemStack stack, World world) {
		final Element element = Optional.ofNullable(ElementalAttunementComponent.get(stack))
			.map(ElementalAttunementComponent::element)
			.orElse(ELEMENTS.get(world.getRandom().nextInt(ELEMENTS.size())));
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
			if (source.isIndirect() || !(target instanceof final LivingEntity livingTarget) || !(source.getAttacker() instanceof final LivingEntity attacker)) return Optional.empty();

			final ElementalInfusionComponent component = ElementalInfusionComponent.get(attacker.getMainHandStack());

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
			ElementalAttunementComponent.hasAttunement(stack)
			&& ElementalAttunementComponent.get(stack).element() != applicationBuilder.getElement()
		) return false;

		final ElementalInfusionComponent component = ElementalInfusionComponent.get(stack);

		if (component == null) return false;

		component.setElementalInfusion(applicationBuilder);
		component.setInternalCooldown(icdBuilder);

		return true;
	}

	public static boolean removeInfusion(ItemStack stack) {
		if (!ElementalInfusionComponent.hasInfusion(stack)) return false;

		final ElementalInfusionComponent component = ElementalInfusionComponent.get(stack);

		if (component == null) return false;

		component.remove("elemental_infusion");
		component.remove("internal_cooldown");

		return true;
	}

	public static boolean hasInfusion(ItemStack stack) {
		return Optional
			.ofNullable(ElementalInfusionComponent.get(stack))
			.map(ElementalInfusionComponent::hasElementalInfusion)
			.orElse(false);
	}

	public static @Nullable ElementalInfusionComponent get(ItemStack stack) {
		return ElementalInfusionComponent.KEY.maybeGet(stack).orElse(null);
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
			? this.elementalInfusion().getGaugeUnits()
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

	@Override
	public void appendTooltip(@Nullable PlayerEntity player, TooltipContext context, Consumer<Text> textConsumer) {
		if (!this.hasElementalInfusion()) return;

		final Builder icdContext = this.internalCooldown();

		textConsumer.accept(
			Text.empty()
				.append(Text.translatable("item.seven-elements.components.infusion.infusion").formatted(Formatting.WHITE))
				.append(ElementalApplication.Builder.getText(this.elementalInfusion()))
		);

		@Nullable InternalCooldownTag tag = ClassInstanceUtil.mapOrNull(icdContext, Builder::getTag);

		final Text tagText = tag != null
			? tag.getText(Formatting.DARK_GRAY)
			: Text.literal("none").formatted(Formatting.RED);

		textConsumer.accept(
			Text.empty()
				.append(Text.translatable("item.seven-elements.components.infusion.tag").formatted(Formatting.WHITE))
				.append(tagText)
		);

		final InternalCooldownType icdType = JavaScriptUtil.nullishCoalesing(
			ClassInstanceUtil.mapOrNull(icdContext, Builder::getType),
			InternalCooldownType.DEFAULT
		);

		textConsumer.accept(
			Text.empty()
				.append(Text.translatable("item.seven-elements.components.infusion.type").formatted(Formatting.WHITE))
				.append(icdType.getText(true).formatted(Formatting.DARK_GRAY))
		);
	}
}
