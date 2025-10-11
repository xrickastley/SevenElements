package io.github.xrickastley.sevenelements.element.reaction;

import com.google.common.base.Suppliers;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.annotation.mixin.At;
import io.github.xrickastley.sevenelements.annotation.mixin.Inject;
import io.github.xrickastley.sevenelements.annotation.mixin.Local;
import io.github.xrickastley.sevenelements.annotation.mixin.ModifyExpressionValue;
import io.github.xrickastley.sevenelements.annotation.mixin.ModifyVariable;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementHolder;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.events.ElementEvents;
import io.github.xrickastley.sevenelements.events.ReactionTriggered;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypes;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistries;

import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.server.world.ServerWorld;

public abstract sealed class AbstractBurningElementalReaction
	extends ElementalReaction
	permits BurningElementalReaction, QuickenBurningElementalReaction
{
	private static final InternalCooldownType BURNING_PYRO_ICD = InternalCooldownType.registered(SevenElements.identifier("reactions/burning/pyro_icd"), 40, Integer.MAX_VALUE);
	private static final Supplier<Set<ElementalReaction>> REACTIONS = Suppliers.memoize(
		() -> SevenElementsRegistries.ELEMENTAL_REACTION
			.streamEntries()
			.map(Reference::value)
			.filter(r -> !(r instanceof AbstractDendroCoreElementalReaction) && !(r.getAuraElement() == Element.PYRO || (r.getTriggeringElement() == Element.PYRO && r.reversable)))
			.collect(Collectors.toSet())
	);

	AbstractBurningElementalReaction(Settings settings) {
		super(settings);
	}

	@Override
	public boolean isTriggerable(LivingEntity entity) {
		return super.isTriggerable(entity) && !ElementComponent.KEY.get(entity).hasElementalApplication(Element.BURNING);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final ElementComponent component = ElementComponent.KEY.get(entity);
		final InternalCooldownContext context = InternalCooldownContext.ofType(origin, "seven-elements:reactions/burning", BURNING_PYRO_ICD);
		final ElementHolder holder = component.getElementHolder(Element.PYRO);

		if (context.hasOrigin() && context.getInternalCooldown(holder).handleInternalCooldown()) {
			final ElementalApplication application = ElementalApplications.gaugeUnits(entity, Element.PYRO, 1.0f, true);

			if (!holder.hasElementalApplication() || holder.getElementalApplication().isEmpty()) {
				holder.setElementalApplication(application);
			} else {
				holder.getElementalApplication().reapply(application);
			}
		}

		component
			.getElementHolder(Element.BURNING)
			.setElementalApplication(
				ElementalApplications.gaugeUnits(entity, Element.BURNING, 2.0f, true)
			);

		component.resetBurningCD();
		component.setOrRetainBurningOrigin(origin);

		ElementComponent.sync(entity);
	}

	static {
		ElementEvents.APPLIED.register((element, application) -> {
			if (application.getEntity().getWorld().isClient || element != Element.BURNING) return;

			final ElementComponent component = ElementComponent.KEY.get(application.getEntity());

			if (component.hasElementalApplication(Element.DENDRO) || component.hasElementalApplication(Element.QUICKEN)) return;

			component
				.getElementHolder(Element.BURNING)
				.setElementalApplication(null);

			ElementComponent.sync(application.getEntity());
		});

		ElementEvents.REMOVED.register((element, application) -> {
			if (element == Element.BURNING) ElementComponent.sync(application.getEntity());

			if (element != Element.DENDRO && element != Element.QUICKEN) return;

			final ElementComponent component = ElementComponent.KEY.get(application.getEntity());

			component
				.getElementHolder(Element.BURNING)
				.setElementalApplication(null);

			ElementComponent.sync(application.getEntity());
		});

		ReactionTriggered.EVENT.register((reaction, reducedGauge, target, origin) -> {
			final ElementComponent component = ElementComponent.KEY.get(target);

			if (!component.hasElementalApplication(Element.BURNING) || reaction instanceof AbstractBurningElementalReaction) return;

			final ElementalApplication applicationAE = component.getElementalApplication(reaction.getAuraElement());
			final ElementalApplication applicationTE = component.getElementalApplication(reaction.getTriggeringElement());

			final double newReducedGauge = (applicationAE.getElement() == Element.PYRO
				? applicationTE.getCurrentGauge() + reducedGauge
				: applicationAE.getCurrentGauge() + reducedGauge) * reaction.reactionCoefficient;

			component
				.getElementalApplication(Element.BURNING)
				.reduceGauge(newReducedGauge);

			ElementComponent.sync(target);
		});
	}

	// These "mixins" are injected pieces of code that allow Burning to work properly, and allow code readers to easily see the way it was hardcoded.
	@Inject(
		method = "Lio/github/xrickastley/sevenelements/component/ElementComponentImpl;tick()V",
		at = @At("HEAD")
	)
	public static void mixin$tick(@Local(field = "owner:Lnet/minecraft/entity/LivingEntity;") LivingEntity entity) {
		if (!(entity.getWorld() instanceof final ServerWorld world)) return;

		final ElementComponent component = ElementComponent.KEY.get(entity);

		if (!component.hasElementalApplication(Element.BURNING) || component.isBurningOnCD() || entity.getWorld().isClient) return;

		if (!component.hasElementalApplication(Element.DENDRO) && !component.hasElementalApplication(Element.QUICKEN)) {
			component
				.getElementHolder(Element.BURNING)
				.setElementalApplication(null);

			return;
		}

		for (final LivingEntity target : ElementalReaction.getEntitiesInAoE(entity, 1, t -> !ElementComponent.KEY.get(t).isBurningOnCD())) {
			final float damage = ElementalReaction.getReactionDamage(entity, 0.25);
			final ElementalDamageSource source = new ElementalDamageSource(
				entity
					.getDamageSources()
					.create(SevenElementsDamageTypes.BURNING, entity, component.getBurningOrigin()),
				target == entity
					? ElementalApplications.gaugeUnits(target, Element.PYRO, 0)
					: ElementalApplications.gaugeUnits(target, Element.PYRO, 1),
				target == entity
					? InternalCooldownContext.ofNone()
					: InternalCooldownContext.ofType(entity, "seven-elements:reactions/burning", BURNING_PYRO_ICD)
			).shouldApplyDMGBonus(false);

			target.damage(world, source, damage);
			target.setOnFire(true);
			target.setFireTicks(5);

			final ElementComponent targetComponent = ElementComponent.KEY.get(target);
			final ElementHolder holder = targetComponent.getElementHolder(Element.PYRO);

			if (target == entity && holder.canApplyElement(Element.PYRO, InternalCooldownContext.ofType(entity, "seven-elements:reactions/burning", BURNING_PYRO_ICD), true)) {
				final ElementalApplication application = holder.getElementalApplication();

				if (application == null) {
					holder.setElementalApplication(ElementalApplications.gaugeUnits(target, Element.PYRO, 1));
				} else {
					application.reapply(Element.PYRO, 1);
				}
			}

			targetComponent.resetBurningCD();
		}
	}

	/**
	 * Reapplies the Dendro element when the only "highest priority" element is Burning. <br> <br>
	 *
	 * This method will <b>overwrite</b> the current Dendro aura with the provided Elemental
	 * Application, as specified by the "Burning Refresh" mechanic by <a href="https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory/Advanced_Mechanics#Burning">
	 * Elemental Gauge Theory > Advanced Mechanics > Burning</a>. <br> <br>
	 *
	 * If the provided application is <i>not</i> the Dendro element, it is ignored.
	 *
	 * @param application The {@code ElementalApplication} to reapply.
	 */
	@Inject(
		method = "Lio/github/xrickastley/sevenelements/component/ElementComponentImpl;attemptReapply(Lio/github/xrickastley/sevenelements/element/ElementalApplication;)Z",
		at = @At(
			value = "IF",
			ordinal = 2
		),
		directive = "INJECT_AS_ELSE"
	)
	public static void mixin$forceReapplyDendroWhenBurning(@Local(self = true) ElementComponent component, @Local(argsOnly = true) ElementalApplication application) {
		if (application.getElement() != Element.DENDRO) return;

		final Set<Element> appliedElements = component
			.getAppliedElements()
			.stream()
			.map(ElementalApplication::getElement)
			.collect(Collectors.toSet());

		if (!appliedElements.contains(Element.BURNING)) return;

		component
			.getElementHolder(Element.DENDRO)
			.setElementalApplication(application.asAura());

		ElementComponent.sync(component.getOwner());
	}

	@ModifyExpressionValue(
		method = "Lio/github/xrickastley/sevenelements/component/ElementComponentImpl;triggerReactions(Lio/github/xrickastley/sevenelements/element/ElementalApplication;Lnet/minecraft/entity/LivingEntity;)Ljava/util/Set;",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/stream/Stream;noneMatch(Ljava/util/function/Predicate;)Z"
		)
	)
	public static boolean mixin$onlyAllowPyroReactions(final boolean original, final @Local(self = true) ElementComponent component, final @Local ElementalReaction reaction) {
		if (!component.hasElementalApplication(Element.BURNING)) return original;

		return original && !REACTIONS.get().contains(reaction);
	}

	@ModifyExpressionValue(
		method = "Lio/github/xrickastley/sevenelements/component/ElementComponentImpl;triggerReactions(Lio/github/xrickastley/sevenelements/element/ElementalApplication;Lnet/minecraft/entity/LivingEntity;)Ljava/util/Set;",
		at = @At(
			value = "CONSTANT_ACCESS",
			target = "hasHigherPriority;Z"
		)
	)
	public static boolean mixin$allowDendroPassthrough(final boolean original, final @Local ElementComponent component, @Local ElementalApplication application) {
		return original
			&& !(component.hasElementalApplication(Element.BURNING) && (application.getElement() == Element.DENDRO || application.getElement() == Element.PYRO));
	}

	@Inject(
		method = "Lio/github/xrickastley/sevenelements/element/ElementalApplication;tick()V",
		at = @At("HEAD")
	)
	public static void mixin$reduceQuickenGauge(final @Local(argsOnly = true) ElementalApplication application) {
		final ElementComponent component = ElementComponent.KEY.get(application.getEntity());

		if (!component.hasElementalApplication(Element.BURNING) || component.hasElementalApplication(Element.DENDRO) || application.getElement() != Element.QUICKEN) return;

		application.reduceGauge(Element.DENDRO.getCustomDecayRate().apply(application).doubleValue());
	}

	@ModifyVariable(
		method = "Lio/github/xrickastley/sevenelements/component/ElementComponentImpl;triggerReactions(Lio/github/xrickastley/sevenelements/element/ElementalApplication;Lnet/minecraft/entity/LivingEntity;)Ljava/util/Set;",
		at = @At(
			value = "STORE",
			ordinal = 3,
			shift = At.Shift.AFTER
		)
	)
	public static Optional<ElementalReaction> mixin$changeReaction(Optional<ElementalReaction> original, final @Local(self = true) ElementComponent component, final @Local(argsOnly = true) ElementalApplication application) {
		if (!component.hasElementalApplication(Element.BURNING)) return original;

		return SevenElementsRegistries.ELEMENTAL_REACTION
			.streamEntries()
			.map(Reference::value)
			.filter(r -> r.isTriggerable(component.getOwner()) && (r.getAuraElement() == Element.PYRO || (r.getTriggeringElement() == Element.PYRO && r.reversable)))
			.min(Comparator.comparing(r -> r.getPriority(application)));
	}
}
