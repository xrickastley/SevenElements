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

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * A special subclass of {@link MobEffect} that is tied to an {@link Element}. <br> <br>
 *
 * This subclass of {@link MobEffect} will persist on the entity it is inflicted on so long as
 * its corresponding element is applied onto the entity.
 */
public abstract sealed class ElementalStatusEffect
	extends MobEffect
	permits CryoStatusEffect, FrozenStatusEffect
{
	private static final Map<Element, ElementalStatusEffect> ELEMENT_EFFECTS = new HashMap<>();
	private final Element element;

	ElementalStatusEffect(MobEffectCategory category, int color, Element element) {
		super(category, color);

		if (ELEMENT_EFFECTS.containsKey(element))
			throw new IllegalArgumentException("An ElementalStatusEffect for the provided element: " + element + " already exists: " + ELEMENT_EFFECTS.get(element).getClass().getName());

		this.element = element;

		ElementalStatusEffect.ELEMENT_EFFECTS.put(element, this);
	}

	public static @Nullable ElementalStatusEffect getEffectForElement(Element element) {
		return ElementalStatusEffect.ELEMENT_EFFECTS.get(element);
	}

	public static @Nullable Holder<MobEffect> getEntryForElement(Element element) {
		final ElementalStatusEffect effect = ElementalStatusEffect.ELEMENT_EFFECTS.get(element);

		if (effect == null) return null;

		return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
	}

	public static List<Holder<MobEffect>> getElementEffects() {
		return ElementalStatusEffect.ELEMENT_EFFECTS
			.values()
			.stream()
			.map(BuiltInRegistries.MOB_EFFECT::wrapAsHolder)
			.toList();
	}

	public static Optional<ElementalStatusEffect> asElementEffect(Holder<MobEffect> effect) {
		return Optional.ofNullable(ClassInstanceUtil.castOrNull(effect.value(), ElementalStatusEffect.class));
	}

	public static void applyPossibleStatusEffect(ElementalApplication application) {
		final ElementalStatusEffect effect = ElementalStatusEffect.getEffectForElement(application.getElement());

		if (effect == null) return;

		effect.applyStatusEffect(application);
	}

	public static boolean isElementalEffect(Holder<MobEffect> effect) {
		return effect.value() instanceof ElementalStatusEffect;
	}

	public Element getElement() {
		return element;
	}

	public void applyStatusEffect(ElementalApplication application) {
		if (application.getElement() != this.element) return;

		application.getEntity().addEffect(
			new MobEffectInstance(
				BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this),
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
			if (application.getEntity() instanceof final ServerPlayer player && player.connection == null) return;

			ClassInstanceUtil.ifPresentMapped(element, ElementalStatusEffect::getEffectForElement, Functions.withArgument(ElementalStatusEffect::applyStatusEffect, application));
		}

		@Override
		public void onElementReapplied(Element element, ElementalApplication result) {
			if (result.getEntity() instanceof final ServerPlayer player && player.connection == null) return;

			ClassInstanceUtil.ifPresentMapped(element, ElementalStatusEffect::getEffectForElement, Functions.withArgument(ElementalStatusEffect::applyStatusEffect, result));
		}

		@Override
		public void onElementRefreshed(Element element, ElementalApplication current, ElementalApplication previous) {
			if (current.getEntity() instanceof final ServerPlayer player && player.connection == null) return;

			ClassInstanceUtil.ifPresentMapped(element, ElementalStatusEffect::getEffectForElement, Functions.withArgument(ElementalStatusEffect::applyStatusEffect, current));
		}

		@Override
		public void onElementRemoved(Element element, ElementalApplication application) {
			if (application.getEntity() instanceof final ServerPlayer player && player.connection == null) return;

			ClassInstanceUtil.ifPresentMapped(element, ElementalStatusEffect::getEffectForElement, Functions.composeConsumer(BuiltInRegistries.MOB_EFFECT::wrapAsHolder, application.getEntity()::removeEffect));
		}
	}
}
