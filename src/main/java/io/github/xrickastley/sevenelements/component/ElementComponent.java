package io.github.xrickastley.sevenelements.component;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementHolder;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.util.Array;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Pair;

public interface ElementComponent extends AutoSyncedComponent, CommonTickingComponent {
	public static final ComponentKey<ElementComponent> KEY = ComponentRegistry.getOrCreate(SevenElements.identifier("elements"), ElementComponent.class);

	/**
	 * Denies Elemental Applications for the specific entity class. <br> <br>
	 *
	 * The provided entity class, and all its subclasses, cannot have elements applied to them,
	 * either by force or naturally.
	 *
	 * @param entityClass The entity to deny Elemental Applications for.
	 */
	public static <T extends LivingEntity> void denyElementsFor(Class<T> entityClass) {
		ElementComponentImpl.DENIED_ENTITIES.add(ClassInstanceUtil.cast(entityClass));
	}

	public static ElementalDamageSource applyElementalInfusions(DamageSource source, LivingEntity target) {
		if (source instanceof final ElementalDamageSource eds && (eds.getElementalApplication().getElement() != Element.PHYSICAL || !eds.shouldInfuse())) return eds;

		@SuppressWarnings("unchecked")
		final ElementalDamageSource infusion = JavaScriptUtil.nullishCoalesingFn(
			Functions.map(Functions.supplier(ElementalInfusionComponent::applyToDamageSource, source, target), ElementComponent::get),
			Functions.map(Functions.supplier(ElementComponent::attemptDamageTypeInfusions, source, target), ElementComponent::get),
			Functions.map(Functions.supplier(ElementComponent::attemptEntityDamageInfusions, source, target), ElementComponent::get),
			Functions.map(Functions.supplier(ElementComponent::attemptProjectileInfusions, source, target), ElementComponent::get)
		);

		return infusion != null
			? infusion
			: ElementalDamageSource.of(source, target);
	}

	private static Optional<ElementalDamageSource> attemptEntityDamageInfusions(DamageSource source, LivingEntity target) {
		if (!(source.getAttacker() instanceof final LivingEntity attacker)) return Optional.empty();

		for (final var entry : ElementComponentImpl.ENTITY_TYPE_ELEMENT_MAP.entrySet()) {
			if (!attacker.getType().isIn(entry.getKey())) continue;

			return Optional.of(
				new ElementalDamageSource(
					source,
					ElementalApplications.gaugeUnits(target, entry.getValue(), 1.0),
					InternalCooldownContext.ofDefault(attacker, "seven-elements:mob_attack")
				)
			);
		}

		return Optional.empty();
	}

	private static Optional<ElementalDamageSource> attemptDamageTypeInfusions(DamageSource source, LivingEntity target) {
		for (final var entry : ElementComponentImpl.DAMAGE_TYPE_ELEMENT_MAP.entrySet()) {
			if (!source.isIn(entry.getKey())) continue;

			return Optional.of(
				new ElementalDamageSource(
					source,
					ElementalApplications.gaugeUnits(target, entry.getValue(), 1.0),
					InternalCooldownContext.ofDefault(target, "seven-elements:damage_infusion")
				)
			);
		}

		return Optional.empty();
	}

	private static Optional<ElementalDamageSource> attemptProjectileInfusions(DamageSource source, LivingEntity target) {
		// Projectiles are indirect DMG sources.
		if (source.isDirect()) return Optional.empty();

		return source.getSource() instanceof final ProjectileEntity projectile
			? projectile.sevenelements$attemptInfusion(source, target)
			: Optional.empty();
	}

	private static <T> @Nullable T get(Optional<T> optional) {
		return optional.orElse(null);
	}

	public LivingEntity getOwner();

	public ElementHolder getElementHolder(Element element);

	public Pair<ElementalReaction, Long> getLastReaction();

	default boolean hasLastReaction() {
		return this.getLastReaction().getLeft() != null;
	}

	default boolean hasValidLastReaction() {
		return this.hasLastReaction() && this.getLastReaction().getRight() + 10 >= this.getOwner().getEntityWorld().getTime();
	}

	public boolean isElectroChargedOnCD();

	public boolean isBurningOnCD();

	public void resetElectroChargedCD();

	public void resetBurningCD();

	public void setElectroChargedOrigin(@Nullable LivingEntity origin);

	public void setBurningOrigin(@Nullable LivingEntity origin);

	public @Nullable LivingEntity getElectroChargedOrigin();

	public @Nullable LivingEntity getBurningOrigin();

	/**
	 * Gets the current decay time modifier for the Freeze aura. <br> <br>
	 *
	 * Freeze is a special element: its decay is unique.
	 * <ul>
	 * 	<li>Freeze's gauge decay starts with the standard rate: 0.4 GU/s</li>
	 * 	<li>For every second the entity is frozen, the gauge decay increases by 0.1 GU/s, hence 0.1 GU/s² acceleration</li>
	 * 	<li>When the entity is unfrozen, the gauge decay decreases by 0.2 GU/s until it is back at 0.4 GU/s, hence 0.2 GU/s² deceleration</li>
	 * </ul>
	 *
	 * By extending the Freeze formula, we get a generalized Freeze duration formula for consecutive
	 * Frozen reactions. <br> <br>
	 *
	 * {@code sum(max(0, t_frozen_i - 2 t_unfrozen_i))} <br> <br>
	 *
	 * {@code i} is a Frozen and unfrozen instance, and since we reset only when it is 0 due to
	 * being negative, we could drop the {@code sum()} function, as this behaves exactly like the
	 * mathematical model, given that we update the frozenTime and unfrozenTime accordingly. <br> <br>
	 *
	 * Retrieved from: <a href="https://library.keqingmains.com/combat-mechanics/elemental-effects/transformative-reactions#frozen">https://library.keqingmains.com/combat-mechanics/elemental-effects/transformative-reactions#frozen</a>.
	 */
	public double getFreezeDecayTimeModifier();

	default void setOrRetainElectroChargedOrigin(@Nullable LivingEntity origin) {
		this.setElectroChargedOrigin(origin != null ? origin : this.getElectroChargedOrigin());
	}

	default void setOrRetainBurningOrigin(@Nullable LivingEntity origin) {
		this.setBurningOrigin(origin != null ? origin : this.getBurningOrigin());
	}

	/**
	 * Grants this entity a Crystallize shield.
	 * @param element The element of this Crystallize shield.
	 * @param amount The amount of HP this shield has.
	 */
	public void setCrystallizeShield(Element element, double amount);

	/**
	 * Gets the current Crystallize Shield of this entity.
	 */
	public @Nullable Pair<Element, Double> getCrystallizeShield();

	/**
	 * Reduces the Crystallize shield and returns the effective amount of DMG reduced.
	 *
	 * @param source The {@code DamageSource}. If this isn't an instance of {@code ElementalDamageSource}, {@code 0} is returned.
	 * @param amount The amount of damage to be dealt to the entity.
	 */
	public float reduceCrystallizeShield(DamageSource source, float amount);

	/**
	 * Returns whether the Crystallize Shield has been reduced in the current tick.
	 */
	public boolean reducedCrystallizeShield();

	/**
	 * Checks if the element can be applied.
	 * @param element The element to check.
	 * @param icdContext The {@link InternalCooldownContext} of the Element to be applied.
	 */
	default boolean canApplyElement(Element element, InternalCooldownContext icdContext) {
		return this.canApplyElement(element, icdContext, false);
	}

	/**
	 * Checks if the element can be applied.
	 * @param element The element to check.
	 * @param icdContext The {@link InternalCooldownContext} of the Element to be applied.
	 * @param handleICD Whether the ICD should be handled. This will register a "hit" to the gauge sequence.
	 */
	public boolean canApplyElement(Element element, InternalCooldownContext icdContext, boolean handleICD);

	default List<ElementalReaction> addElementalApplication(Element element, InternalCooldownContext icdContext, double gaugeUnits) {
		final boolean isAura = this.getAppliedElements().isEmpty();

		return this.addElementalApplication(ElementalApplications.gaugeUnits(this.getOwner(), element, gaugeUnits, isAura), icdContext);
	}

	default List<ElementalReaction> addElementalApplication(Element element, InternalCooldownContext icdContext, double gaugeUnits, double duration) {
		return this.addElementalApplication(ElementalApplications.duration(this.getOwner(), element, gaugeUnits, duration), icdContext);
	}

	public List<ElementalReaction> addElementalApplication(ElementalApplication application, InternalCooldownContext icdContext);

	/**
	 * Checks if this entity has a specified Elemental Application with the provided {@code element}.
	 * @param element The element to check.
	 * @return Whether the entity has the specified element applied.
	 */
	default boolean hasElementalApplication(Element element) {
		return this
			.getElementHolder(element)
			.hasElementalApplication();
	}

	/**
	 * Reduces the amount of gauge units in a specified element, then returns the eventual amount of gauge units reduced.
	 * @param element The element to reduce the gauge units of.
	 * @param gaugeUnits The amount of gauge units to reduce.
	 * @return The eventual amount of gauge units reduced. If this value is lower than {@code gaugeUnits}, the current
	 * element had a current gauge value lesser than {@code gaugeUnits}. However, if this value is {@code -1.0}, the provided
	 * {@code element} was not found or did not exist.
	 *
	 * @see ElementalApplication#reduceGauge
	 */
	default double reduceElementalApplication(Element element, double gaugeUnits) {
		return Optional.ofNullable(this.getElementalApplication(element))
			.map(application -> application.reduceGauge(gaugeUnits))
			.orElse(-1.0);
	}

	/**
	 * Gets an Elemental Application with the specified {@code element}.
	 * @param element The {@code Element} to get an Elemental Application from.
	 * @return The {@code ElementalApplication}, if one exists for {@code element}.
	 */
	default ElementalApplication getElementalApplication(Element element) {
		return this
			.getElementHolder(element)
			.getElementalApplication();
	}

	/**
	 * Gets all currently applied elements as a {@link Array}.
	 */
	public Array<ElementalApplication> getAppliedElements();

	/**
	 * Applies an {@link ElementalDamageSource} to this entity, <i>possibly</i> triggering
	 * multiple {@link ElementalReaction}s. If no reactions were triggered, the list will be empty.
	 *
	 * @param source The {@code ElementalDamageSource} to apply to this entity.
	 * @return The triggered {@link ElementalReaction}s.
	 */
	public List<ElementalReaction> applyFromDamageSource(final ElementalDamageSource source);

	/**
	 * Gets the lowest {@code priority} value from the currently applied Elements
	 * as an {@link Optional}. <br> <br>
	 *
	 * If the {@code Optional} has no value, this means that there are no Elements
	 * currently applied.
	 */
	public Optional<Integer> getHighestElementPriority();

	/**
	 * Gets all currently prioritized applied elements as an {@link Array}. <br> <br>
	 *
	 * If there are applied Elements with multiple priority values, the most
	 * prioritized one has to be consumed first before the others can be consumed. <br> <br>
	 *
	 * Say that Element A has a priority of {@code 1}, while Element B has a priority
	 * of {@code 2}. Element A's application must be consumed entirely before Element B
	 * could be reacted with or reapplied.
	 */
	public Array<ElementalApplication> getPrioritizedElements();

	public static void sync(Entity entity) {
		if (entity.getEntityWorld().isClient()) return;

		ElementComponent.KEY.sync(entity);
	}
}
