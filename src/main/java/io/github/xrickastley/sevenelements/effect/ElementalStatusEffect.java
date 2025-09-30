package io.github.xrickastley.sevenelements.effect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.events.ElementEvents.ElementApplied;
import io.github.xrickastley.sevenelements.events.ElementEvents.ElementReapplied;
import io.github.xrickastley.sevenelements.events.ElementEvents.ElementRefreshed;
import io.github.xrickastley.sevenelements.events.ElementEvents.ElementRemoved;
import io.github.xrickastley.sevenelements.events.ElementEvents;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * A special subclass of {@link StatusEffect} that is tied to an {@link Element}. <br> <br>
 *
 * This subclass of {@link StatusEffect} will persist on the entity it is inflicted on so long as
 * its corresponding element is applied onto the entity.
 */
public abstract sealed class ElementalStatusEffect
	extends StatusEffect
	permits CryoStatusEffect, FrozenStatusEffect
{
	private static final Map<Element, ElementalStatusEffect> ELEMENT_EFFECTS = new HashMap<>();
	private final Element element;

	ElementalStatusEffect(StatusEffectCategory category, int color, Element element) {
		super(category, color);

		if (ELEMENT_EFFECTS.containsKey(element))
			throw new IllegalArgumentException("An ElementalStatusEffect for the provided element: " + element + " already exists: " + ELEMENT_EFFECTS.get(element).getClass().getName());

		this.element = element;

		ElementalStatusEffect.ELEMENT_EFFECTS.put(element, this);
	}

	public static @Nullable ElementalStatusEffect getEffectForElement(Element element) {
		return ElementalStatusEffect.ELEMENT_EFFECTS.get(element);
	}

	public static @Nullable RegistryEntry<StatusEffect> getEntryForElement(Element element) {
		final ElementalStatusEffect effect = ElementalStatusEffect.ELEMENT_EFFECTS.get(element);

		if (effect == null) return null;

		return Registries.STATUS_EFFECT.getEntry(effect);
	}

	public static List<RegistryEntry<StatusEffect>> getElementEffects() {
		return ElementalStatusEffect.ELEMENT_EFFECTS
			.values()
			.stream()
			.map(Registries.STATUS_EFFECT::getEntry)
			.toList();
	}

	public static Optional<ElementalStatusEffect> asElementEffect(RegistryEntry<StatusEffect> effect) {
		return Optional.ofNullable(ClassInstanceUtil.castOrNull(effect.value(), ElementalStatusEffect.class));
	}

	public static void applyPossibleStatusEffect(ElementalApplication application) {
		final ElementalStatusEffect effect = ElementalStatusEffect.getEffectForElement(application.getElement());

		if (effect == null) return;

		effect.applyStatusEffect(application);
	}

	public static boolean isElementalEffect(RegistryEntry<StatusEffect> effect) {
		return effect.value() instanceof ElementalStatusEffect;
	}

	public Element getElement() {
		return element;
	}

	public void applyStatusEffect(ElementalApplication application) {
		if (application.getElement() != this.element) return;

		application.getEntity().addStatusEffect(
			new StatusEffectInstance(
				Registries.STATUS_EFFECT.getEntry(this),
				application.getRemainingTicks(),
				0,
				true,
				false,
				true
			)
		);
	}

	static {
		ElementEvents.APPLIED.register(Handler.INSTANCE);
		ElementEvents.REAPPLIED.register(Handler.INSTANCE);
		ElementEvents.REFRESHED.register(Handler.INSTANCE);
		ElementEvents.REMOVED.register(Handler.INSTANCE);
	}

	private static class Handler implements ElementApplied, ElementReapplied, ElementRefreshed, ElementRemoved {
		private static final Handler INSTANCE = new Handler();

		@Override
		public void onElementApplied(Element element, ElementalApplication application) {
			if (application.getEntity() instanceof final ServerPlayerEntity player && player.networkHandler == null) return;

			ClassInstanceUtil.ifPresentMapped(element, ElementalStatusEffect::getEffectForElement, Functions.withArgument(ElementalStatusEffect::applyStatusEffect, application));
		}

		@Override
		public void onElementReapplied(Element element, ElementalApplication result) {
			if (result.getEntity() instanceof final ServerPlayerEntity player && player.networkHandler == null) return;

			ClassInstanceUtil.ifPresentMapped(element, ElementalStatusEffect::getEffectForElement, Functions.withArgument(ElementalStatusEffect::applyStatusEffect, result));
		}

		@Override
		public void onElementRefreshed(Element element, ElementalApplication current, ElementalApplication previous) {
			if (current.getEntity() instanceof final ServerPlayerEntity player && player.networkHandler == null) return;

			ClassInstanceUtil.ifPresentMapped(element, ElementalStatusEffect::getEffectForElement, Functions.withArgument(ElementalStatusEffect::applyStatusEffect, current));
		}

		@Override
		public void onElementRemoved(Element element, ElementalApplication application) {
			if (application.getEntity() instanceof final ServerPlayerEntity player && player.networkHandler == null) return;

			ClassInstanceUtil.ifPresentMapped(element, ElementalStatusEffect::getEffectForElement, Functions.composeConsumer(Registries.STATUS_EFFECT::getEntry, application.getEntity()::removeStatusEffect));
		}
	}
}
