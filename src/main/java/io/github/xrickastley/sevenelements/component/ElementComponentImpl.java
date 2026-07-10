package io.github.xrickastley.sevenelements.component;

import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementHolder;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.reaction.AbstractBurningElementalReaction;
import io.github.xrickastley.sevenelements.element.reaction.ElectroChargedElementalReaction;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.element.reaction.FrozenElementalReaction;
import io.github.xrickastley.sevenelements.element.reaction.QuickenElementalReaction;
import io.github.xrickastley.sevenelements.events.ElementEvents;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypeTags;
import io.github.xrickastley.sevenelements.registry.SevenElementsEntityTypeTags;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistries;
import io.github.xrickastley.sevenelements.util.Array;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.ImmutablePair;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;
import io.github.xrickastley.sevenelements.util.NbtHelper;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public final class ElementComponentImpl implements ElementComponent {
	static final Set<Class<LivingEntity>> DENIED_ENTITIES = new HashSet<>();
	static final Map<TagKey<EntityType<?>>, Element> ENTITY_TYPE_ELEMENT_MAP = new HashMap<>();
	static final Map<TagKey<DamageType>, Element> DAMAGE_TYPE_ELEMENT_MAP = new HashMap<>();
	static final Map<Integer, InfusionFunction> INFUSION_FUNCTIONS = new HashMap<>();

	private final LivingEntity owner;
	private final Map<Element, ElementHolder> elementHolders = new ConcurrentHashMap<>();
	private final Map<Identifier, Integer> mechanicPityHolder = new ConcurrentHashMap<>();
	private final FreezeDecayHandler freezeDecayHandler;
	private Pair<ElementalReaction, Long> lastReaction = new Pair<>(null, -1L);
	private long electroChargedCooldown = -1;
	private @Nullable LivingEntity electroChargedOrigin = null;
	private long burningCooldown = -1;
	private @Nullable LivingEntity burningOrigin = null;
	private CrystallizeShield crystallizeShield = null;
	private int crystallizeShieldReducedAt = -1;

	@ApiStatus.Internal
	public static <T extends LivingEntity> boolean canApplyElement(Class<T> entityClass) {
		return !ElementComponentImpl.DENIED_ENTITIES.contains(entityClass);
	}

	public ElementComponentImpl(LivingEntity owner) {
		this.owner = owner;
		this.freezeDecayHandler = new FreezeDecayHandler();

		for (final Element element : Element.values()) elementHolders.put(element, ElementHolder.of(owner, element));
	}

	@Override
	public boolean isElectroChargedOnCD() {
		return this.owner.getWorld().getTime() < this.electroChargedCooldown;
	}

	@Override
	public boolean isBurningOnCD() {
		return this.owner.getWorld().getTime() < this.burningCooldown;
	}

	@Override
	public void resetElectroChargedCD() {
		this.electroChargedCooldown = this.owner.getWorld().getTime() + 20;
	}

	@Override
	public void resetBurningCD() {
		this.burningCooldown = this.owner.getWorld().getTime() + 5;
	}

	@Override
	public void setElectroChargedOrigin(@Nullable LivingEntity origin) {
		this.electroChargedOrigin = origin;
	}

	@Override
	public void setBurningOrigin(@Nullable LivingEntity origin) {
		this.burningOrigin = origin;
	}

	@Override
	public @Nullable LivingEntity getElectroChargedOrigin() {
		return this.electroChargedOrigin;
	}

	public @Nullable LivingEntity getBurningOrigin() {
		return this.burningOrigin;
	}

	@Override
	public double getFreezeDecayTimeModifier() {
		return this.freezeDecayHandler.getDecayTimeModifier();
	}

	@Override
	public void setCrystallizeShield(Element element, double amount) {
		this.crystallizeShield = new CrystallizeShield(element, amount, this.owner.getWorld().getTime());

		ElementComponent.sync(owner);
	}

	@Override
	public @Nullable Pair<Element, Double> getCrystallizeShield() {
		return this.crystallizeShield == null
			? null
			: new Pair<>(this.crystallizeShield.element, this.crystallizeShield.amount);
	}

	@Override
	public float reduceCrystallizeShield(DamageSource source, float amount) {
		if (this.crystallizeShield == null) return 0;

		final ElementalDamageSource eds = JavaScriptUtil.nullishCoalesing(
			ClassInstanceUtil.castOrNull(source, ElementalDamageSource.class),
			ElementalDamageSource.of(source, this.owner)
		);

		final float reduced = this.crystallizeShield.reduce(eds, amount);

		if (reduced > 0) this.crystallizeShieldReducedAt = this.owner.age;

		if (this.crystallizeShield == null || this.crystallizeShield.isEmpty()) {
			this.owner.getWorld()
				.playSound(null, this.owner.getBlockPos(), SevenElementsSoundEvents.CRYSTALLIZE_SHIELD_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);
		}

		return reduced;
	}

	@Override
	public boolean reducedCrystallizeShield() {
		return this.crystallizeShieldReducedAt == this.owner.age;
	}

	@Override
	public LivingEntity getOwner() {
		return this.owner;
	}

	@Override
	public ElementHolder getElementHolder(Element element) {
		return this.elementHolders.computeIfAbsent(element, e -> ElementHolder.of(owner, e));
	}

	@Override
	public Pair<ElementalReaction, Long> getLastReaction() {
		return ImmutablePair.of(this.lastReaction);
	}

	@ApiStatus.Internal
	public void setLastReaction(Pair<ElementalReaction, Long> lastReaction) {
		this.lastReaction = lastReaction;
	}

	@Override
	public boolean canApplyElement(Element element, InternalCooldownContext icdContext, boolean handleICD) {
		if (element.bypassesInternalCooldown() || !icdContext.hasInternalCooldown()) return true;

		return this.getElementHolder(element).canApplyElement(element, icdContext, true)
			&& ElementComponentImpl.DENIED_ENTITIES.stream().noneMatch(c -> c.isInstance(owner));
	}

	@Override
	public List<ElementalReaction> addElementalApplication(ElementalApplication application, InternalCooldownContext icdContext) {
		// Only do this on the server || Only do this when doElements is true.
		if (!(application.getEntity().getWorld() instanceof final ServerWorld world)
			|| !world.getGameRules().getBoolean(SevenElementsGameRules.DO_ELEMENTS)) return Collections.emptyList();

		if (application.isGaugeUnits() && !application.isAuraElement() && this.getAppliedElements().isEmpty())
			application = application.asAura();

		if (application.isGaugeUnits() && application.isAuraElement() && !this.getAppliedElements().isEmpty())
			application = application.asNonAura();

		// The elemental application is empty.
		if (application.isEmpty()) return Collections.emptyList();

		// The Element is still in ICD.
		if (!this.canApplyElement(application.getElement(), icdContext, true)) return Collections.emptyList();

		// Element has been reapplied, no reactions are triggered.
		if (this.attemptReapply(application)) return Collections.emptyList();

		final Set<ElementalReaction> triggeredReactions = this.triggerReactions(application, icdContext.getOrigin());

		ElementComponent.sync(owner);

		return new ArrayList<>(triggeredReactions);
	}

	@Override
	public Array<ElementalApplication> getAppliedElements() {
		return new Array<>(
			elementHolders
				.values().stream()
				.map(ElementHolder::getElementalApplication)
				.filter(application -> application != null && !application.isEmpty())
		);
	}

	@Override
	public List<ElementalReaction> applyFromDamageSource(ElementalDamageSource source) {
		return addElementalApplication(source.getElementalApplication(), source.getIcdContext());
	}

	@Override
	public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registry) {
		final NbtList list = new NbtList();

		this.getAppliedElements()
			.forEach(application -> list.add(application.asNbt()));

		tag.put("AppliedElements", list);
		tag.putLong("SyncedAt", owner.getWorld().getTime());
		tag.putLong("ElectroChargedCooldown", electroChargedCooldown);
		tag.putLong("BurningCooldown", burningCooldown);

		final NbtCompound freezeDecayHandler = new NbtCompound();
		this.freezeDecayHandler.writeToNbt(freezeDecayHandler);
		tag.put("FreezeDecay", freezeDecayHandler);

		if (this.lastReaction.getLeft() != null) {
			final NbtCompound lastReaction = new NbtCompound();

			lastReaction.putString("Id", this.lastReaction.getLeft().getId().toString());
			lastReaction.putLong("Time", this.lastReaction.getRight());

			tag.put("LastReaction", lastReaction);
		}

		if (this.crystallizeShield != null && !this.crystallizeShield.isEmpty())
			crystallizeShield.writeToNbt(tag);

		if (!this.mechanicPityHolder.isEmpty()) {
			final NbtCompound mechanicPityHolders = new NbtCompound();

			for (final Map.Entry<Identifier, Integer> holder : this.mechanicPityHolder.entrySet())
				mechanicPityHolders.putInt(holder.getKey().toString(), holder.getValue());

			tag.put("PityHolders", mechanicPityHolders);
		}
	}

	@Override
	public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registry) {
		this.electroChargedCooldown = NbtHelper.get(tag, "ElectroChargedCooldown", Codec.LONG);
		this.burningCooldown = NbtHelper.get(tag, "BurningCooldown", Codec.LONG);

		tag.getCompound("LastReaction").ifPresent(lastReaction -> {
			this.lastReaction = new Pair<>(
				SevenElementsRegistries.ELEMENTAL_REACTION.get(NbtHelper.get(lastReaction, "Id", Identifier.CODEC)),
				NbtHelper.get(lastReaction, "Time", Codec.LONG)
			);
		});

		this.crystallizeShield = tag.getCompound("CrystallizeShield")
			.map(CrystallizeShield::new)
			.orElse(null);

		final NbtList list = tag.getListOrEmpty("AppliedElements");
		final long syncedAt = NbtHelper.get(tag, "SyncedAt", Codec.LONG);

		this.elementHolders
			.values()
			.forEach(holder -> holder.setElementalApplication(null));

		for (final NbtElement nbt : list) {
			if (!(nbt instanceof final NbtCompound compound)) return;

			final ElementalApplication application = ElementalApplications.fromNbt(owner, compound, syncedAt);

			this.getElementHolder(application.getElement())
				.setElementalApplication(application);
		}

		this.freezeDecayHandler.readFromNbt(
			tag.getCompound("FreezeDecay"),
			this.owner.getWorld().getTime() - syncedAt
		);

		tag.getCompound("PityHolders").ifPresent(mechanicPityHolders -> {
			this.mechanicPityHolder.clear();

			for (final String holderKey : mechanicPityHolders.getKeys())
				this.mechanicPityHolder.put(Identifier.of(holderKey), mechanicPityHolders.getInt(holderKey).get());
		});
 	}

	@Override
	public void tick() {
		ElectroChargedElementalReaction.mixin$tick(this.owner, this);
		AbstractBurningElementalReaction.mixin$tick(this.owner, this);

		final Array<ElementalApplication> appliedElements = this.getAppliedElements();

		final int tickedElements = appliedElements
			.peek(ElementalApplication::tick)
			.length();

		if (tickedElements > 0) this.removeConsumedElements();

		if (this.crystallizeShield != null)
			crystallizeShield.tick();

		this.freezeDecayHandler.tick(appliedElements.anyMatch(a -> a.getElement() == Element.FREEZE));
	}

	private void removeConsumedElements() {
		final boolean hasRemovedElements = elementHolders
			.values().stream()
			.filter(ElementHolder::hasElementalApplication)
			.anyMatch(ec -> ec.getElementalApplication().isEmpty());

		if (hasRemovedElements) ElementComponent.sync(owner);
	}

	@Override
	public Optional<Integer> getHighestElementPriority() {
		return this
			.getAppliedElements()
			.sortElements((a, b) -> a.getElement().getPriority() - b.getElement().getPriority())
			.findFirst()
			.map(application -> application.getElement().getPriority());
	}

	public Array<ElementalApplication> getPrioritizedElements() {
		final Optional<Integer> priority = this.getHighestElementPriority();

		return priority
			.map(integer -> new Array<>(
				this.getAppliedElements()
					.filter(application -> !application.isEmpty() && application.getElement().getPriority() == integer)
			))
			.orElseGet(Array::new);
	}

	private Stream<ElementalReaction> getTriggerableReactions(int priority, ElementalApplication triggeringElement) {
		final Array<Element> validElements = this.getAppliedElements()
			.filter(application -> application.getElement().getPriority() == priority)
			.map(ElementalApplication::getElement);

		return SevenElementsRegistries.ELEMENTAL_REACTION
			.streamEntries()
			.map(Reference::value)
			.filter(reaction -> reaction.isTriggerable(owner) && reaction.hasAnyElement(validElements) && reaction.getHighestElementPriority() == priority)
			.sorted(Comparator.comparing(reaction -> reaction.getPriority(triggeringElement)));
	}

	/**
	 * Attempts to reapply an {@link ElementalApplication Elemental Application}. <br> <br>
	 *
	 * This method returns whether the provided Elemental Application was "reapplied" in some way,
	 * where {@code true} means that the element has been "reapplied" and cannot be used in an
	 * Elemental Reaction and {@code false} means that the element has not been "reapplied" and can
	 * be used in an Elemental Reaction. <br> <br>
	 *
	 * This method also does <b>not</b> guarantee that all Elemental Applications provided are
	 * indeed reapplied to their respective Elements, as they can be discarded due to the current
	 * Element priority. <br> <br>
	 *
	 * Elements will <b>only</b> be reapplied if the Element in question has the same priority as
	 * the current "highest element priority".
	 *
	 * @param application The {@code ElementalApplication} to reapply.
	 *
	 * @return {@code true} if the Elemental Application was "reapplied", {@code false} otherwise.
	 */
	private boolean attemptReapply(ElementalApplication application) {
		final ElementalApplication currentApplication = this.getElementalApplication(application.getElement());

		if (QuickenElementalReaction.mixin$preventReapplication(application, this)) return false;

		if (currentApplication != null && !currentApplication.isEmpty() && application.getElement().canBeAura()) {
			Optional<Integer> priority = this.getHighestElementPriority();

			if (priority.isEmpty() || priority.get() == currentApplication.getElement().getPriority()) {
				currentApplication.reapply(application);
			} else {
				AbstractBurningElementalReaction.mixin$forceReapplyDendroWhenBurning(this, application);
			}

			return true;
		} else return false;
	}

	/**
	 * Triggers all possible Elemental Reactions. <br> <br>
	 *
	 * Elemental Reactions adhere to the rules of Element priority, where only triggerable
	 * reactions containing elements with the highest priority are considered. <br> <br>
	 *
	 * <h3>Triggering a Reaction</h3>
	 *
	 * When an element is already applied to this entity and another element is applied, an
	 * Elemental Reaction may be triggered. <br> <br>
	 *
	 * Each registered Elemental Reaction is filtered based on the priorities of the elements
	 * participating in that reaction, where reactions only containing elements with the same
	 * priority are considered. <br> <br>
	 *
	 * After that, the candidate reactions are once again sorted based on the currently applied
	 * element's priority. The reaction with the highest priority for the currently applied
	 * element will be the triggered reaction.
	 *
	 * <h3>Triggering Multiple Reactions</h3>
	 *
	 * After a reaction is triggered, so long as the currently applied element still contains
	 * leftover Gauge Units, an attempt to find another reaction that can be triggered is made.
	 * If a reaction is found, it is then triggered and the cycle repeats. <br> <br>
	 *
	 * <h3>Priority Upgrade</h3>
	 *
	 * If no reactions can be triggered, an attempt to upgrade the priority is made first, so
	 * long as the previous reaction allows it, where the newer priority must be greater than the
	 * previous one. <br> <br>
	 *
	 * Once the priority upgrade succeeds, an attempt is made again to find a triggerable reaction.
	 * If a reaction is found, it is then triggered and the cycle repeats with the higher
	 * priority. Otherwise, no more attempts are made to trigger reactions afterward.
	 *
	 * <h3>Applying as an Aura Element</h3>
	 *
	 * Normally, the triggering element is removed when at least one reaction has been triggered.
	 * However, the triggering element can be applied as an aura element afterward if all
	 * participating reactions have {@link ElementalReaction#shouldApplyResultAsAura() ElementalReaction#shouldApplyResultAsAura}
	 * enabled. <br> <br>
	 *
	 * Do note that only {@code GAUGE_UNIT} Elemental Applications are subject to removal.
	 * {@code DURATION} Elemental Applications are not removed or accounted for by this method.
	 *
	 * @param application The {@link ElementalApplication} to apply to this entity.
	 * @param origin The origin of the {@link ElementalApplication}.
	 */
	private Set<ElementalReaction> triggerReactions(ElementalApplication application, @Nullable LivingEntity origin) {
		final Optional<Integer> optionalPriority = this.getHighestElementPriority();
		final ElementHolder context = this.getElementHolder(application.getElement());

		context.setElementalApplication(application);

		// At least one element must be applied for a priority to exist; no priority, no applied element.
		if (optionalPriority.isEmpty()) {
			if (!context.getElement().canBeAura()) context.setElementalApplication(null);

			return Collections.emptySet();
		}

		int priority = Math.min(optionalPriority.get(), application.getElement().getPriority());

		Optional<ElementalReaction> optional = this
			.getTriggerableReactions(priority, application)
			.filter(reaction -> reaction.hasElement(application.getElement()))
			.findFirst();

		optional = AbstractBurningElementalReaction.mixin$changeReaction(optional, this, application);

		boolean applyElementAsAura = true;
		final Set<ElementalReaction> triggeredReactions = new LinkedHashSet<>();

		while (optional.isPresent() && (application.getCurrentGauge() > 0 || optional.get().isTriggerable(owner))) {
			final ElementalReaction reaction = optional.get();

			reaction.trigger(owner, origin);
			applyElementAsAura = applyElementAsAura && reaction.shouldApplyResultAsAura();

			triggeredReactions.add(reaction);

			if (reaction.shouldEndReactionTrigger()) break;

			optional = this
				.getTriggerableReactions(priority, application)
				.filter(r -> FrozenElementalReaction.mixin$modifySwirlReactions(AbstractBurningElementalReaction.mixin$onlyAllowPyroReactions(triggeredReactions.stream().noneMatch(r2 -> r2.idEquals(r)), this, r), this, r))
				.filter(Predicate.not(reaction::preventsReaction))
				.findFirst();

			if (optional.isEmpty() && !reaction.shouldPreventPriorityUpgrade()) {
				final int newPriority = this.getHighestElementPriority().orElse(-1);

				if (newPriority == -1 || newPriority >= priority) break;

				priority = newPriority;
				optional = this
					.getTriggerableReactions(priority, application)
					.filter(r -> FrozenElementalReaction.mixin$modifySwirlReactions(AbstractBurningElementalReaction.mixin$onlyAllowPyroReactions(triggeredReactions.stream().noneMatch(r2 -> r2.idEquals(r)), this, r), this, r))
					.findFirst();
			}
		}

		final Optional<ElementalReaction> firstReaction = triggeredReactions.stream().findFirst();

		firstReaction.ifPresent(elementalReaction -> this.lastReaction = new Pair<>(elementalReaction, this.owner.getWorld().getTime()));

		final boolean cantBeAura = !context.getElement().canBeAura();
		final boolean hasTriggeredReactions = !triggeredReactions.isEmpty();
		final boolean isGaugeUnits = application.isGaugeUnits();
		final boolean hasHigherPriority = this.getHighestElementPriority().orElse(Integer.MIN_VALUE) < application.getElement().getPriority();

		if (cantBeAura || (hasTriggeredReactions && !applyElementAsAura && isGaugeUnits) || AbstractBurningElementalReaction.mixin$allowDendroPassthrough(hasHigherPriority, this, application)) {
			context.setElementalApplication(null);
		} else if (isGaugeUnits) {
			context.setElementalApplication(application.asAura());
		}

		return triggeredReactions;
	}



	@ApiStatus.Internal
	public void resetPityCounter(Identifier id) {
		this.mechanicPityHolder.remove(id);
	}

	@ApiStatus.Internal
	public void incrementPityCounter(Identifier id) {
		this.incrementPityCounter(id, 1);
	}

	@ApiStatus.Internal
	public void incrementPityCounter(Identifier id, int increment) {
		this.mechanicPityHolder.merge(id, increment, Integer::sum);
	}

	@ApiStatus.Internal
	public double getChanceFromPityCounter(Identifier id, double baseChance, int pityStart, double chancePerPity) {
		final int pity = this.mechanicPityHolder.getOrDefault(id, 0);

		return baseChance + Math.max(pity - pityStart, 0) * chancePerPity;
	}



	private class CrystallizeShield {
		private final Element element;
		private final long appliedAt;
		private double amount;

		private CrystallizeShield(final NbtCompound tag) {
			this(
				NbtHelper.get(tag, "Element", Element.CODEC),
				NbtHelper.get(tag, "Amount", Codec.DOUBLE),
				NbtHelper.get(tag, "AppliedAt", Codec.LONG)
			);
		}

		private CrystallizeShield(final Element element, final double amount, final long appliedAt) {
			this.element = element;
			this.appliedAt = appliedAt;
			this.amount = amount;
		}

		private float reduce(ElementalDamageSource source, float amount) {
			final double shieldStrength = 1 + (ElementComponentImpl.this.owner.getAttributeValue(SevenElementsAttributes.SHIELD_STRENGTH) / 100);
			// final double shieldStrength = 1 +
			final double elementBonus = this.element == Element.GEO
				? 1.5 // 150% "effectiveness"
				: source.getElementalApplication().getElement() == this.element
					? 2.5 // 250% "effectiveness"
					: 1; // No "effectiveness"

			final double dmgTakenByShield = Math.min(this.amount * elementBonus * shieldStrength, amount);
			// Use Math.max to guarantee >= 0 in case of FP errors.
			this.amount = Math.max(this.amount - (dmgTakenByShield / (elementBonus * shieldStrength)), 0);

			return (float) dmgTakenByShield;
		}

		private void writeToNbt(NbtCompound tag) {
			final NbtCompound crystallizeShield = new NbtCompound();

			crystallizeShield.putString("Element", this.element.toString());
			crystallizeShield.putDouble("Amount", this.amount);
			crystallizeShield.putLong("AppliedAt", this.appliedAt);

			tag.put("CrystallizeShield", crystallizeShield);
		}

		private boolean isEmpty() {
			return this.amount <= 0 || this.element == null;
		}

		private void tick() {
			final ElementComponentImpl impl = ElementComponentImpl.this;

			if ((this.appliedAt + 300 >= impl.owner.getWorld().getTime() && !this.isEmpty()) || impl.crystallizeShield == null) return;

			impl.crystallizeShield = null;

			impl.owner.getWorld()
				.playSound(null, impl.owner.getBlockPos(), SevenElementsSoundEvents.CRYSTALLIZE_SHIELD_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);

			ElementComponent.sync(impl.owner);
		}
	}

	private class FreezeDecayHandler {
		private boolean isFreezeReapplied = false;
		private long freezeReappliedAt;
		private int freezeTicks;
		private int unfreezeTicks;

		private double getDecayTimeModifier() {
			return Math.max(0, freezeTicks - (2 * unfreezeTicks)) / 20.0;
		}

		private void tick(boolean hasFreezeAura) {
			if (!this.isFreezeReapplied) return;

			if (hasFreezeAura) {
				freezeTicks++;
			} else {
				unfreezeTicks++;
			}

			if (freezeTicks - (2 * unfreezeTicks) > 0) return;
			// do once 0

			this.isFreezeReapplied = false;
			this.freezeReappliedAt = 0;
			this.freezeTicks = 0;
			this.unfreezeTicks = 0;

			ElementComponent.sync(ElementComponentImpl.this.owner);
		}

		public void writeToNbt(NbtCompound tag) {
			tag.putBoolean("FreezeReapplied", isFreezeReapplied);
			tag.putLong("FreezeReappliedAt", freezeReappliedAt);
			tag.putInt("FreezeTicks", freezeTicks);
			tag.putInt("UnfreezeTicks", unfreezeTicks);
		}

		public void readFromNbt(Optional<NbtCompound> tag, long syncDiff) {
			if (tag.isEmpty()) return;

			final NbtCompound nbt = tag.get();

			this.isFreezeReapplied = NbtHelper.get(nbt, "FreezeReapplied", Codec.BOOL);
			this.freezeReappliedAt = NbtHelper.get(nbt, "FreezeReappliedAt", Codec.LONG);
			this.freezeTicks = NbtHelper.get(nbt, "FreezeTicks", Codec.intRange(0, Integer.MAX_VALUE));

			final @Nullable ElementalApplication freezeApp = ElementComponentImpl.this.getElementalApplication(Element.FREEZE);
			final int syncUnfrozenTicks = JavaScriptUtil.nullishCoalesing(ClassInstanceUtil.mapOrNull(freezeApp, ElementalApplication::getRemainingTicks), 0);

			this.unfreezeTicks = NbtHelper.get(nbt, "UnfreezeTicks", Codec.intRange(0, Integer.MAX_VALUE))
				+ (int) Math.max(0, syncDiff - syncUnfrozenTicks);
		}

		static {
			ElementEvents.REAPPLIED.register((element, result) -> {
				if (element != Element.FREEZE) return;

				final ElementComponentImpl component = (ElementComponentImpl) ElementComponent.KEY.get(result.getEntity());

				component.freezeDecayHandler.freezeReappliedAt = component.owner.getWorld().getTime();
				component.freezeDecayHandler.isFreezeReapplied = true;
			});
		}
	}

	static class InfusionFunction implements BiFunction<DamageSource, LivingEntity, Optional<ElementalDamageSource>> {
		private final BiFunction<DamageSource, LivingEntity, Optional<ElementalDamageSource>> infusionFn;
		private final int priority;

		InfusionFunction(BiFunction<DamageSource, LivingEntity, Optional<ElementalDamageSource>> infusionFn, int priority) {
			this.infusionFn = infusionFn;
			this.priority = priority;
		}

		@Override
		public Optional<ElementalDamageSource> apply(DamageSource t, LivingEntity u) {
			return this.infusionFn.apply(t, u);
		}

		public Optional<ElementalDamageSource> get(DamageSource t, LivingEntity u) {
			return this.infusionFn.apply(t, u);
		}

		public int getPriority() {
			return this.priority;
		}
	}

	static {
		ElementComponent.denyElementsFor(ArmorStandEntity.class);

		ElementComponentImpl.ENTITY_TYPE_ELEMENT_MAP.put(SevenElementsEntityTypeTags.DEALS_PYRO_DAMAGE, Element.PYRO);
		ElementComponentImpl.ENTITY_TYPE_ELEMENT_MAP.put(SevenElementsEntityTypeTags.DEALS_HYDRO_DAMAGE, Element.HYDRO);
		ElementComponentImpl.ENTITY_TYPE_ELEMENT_MAP.put(SevenElementsEntityTypeTags.DEALS_ELECTRO_DAMAGE, Element.ELECTRO);
		ElementComponentImpl.ENTITY_TYPE_ELEMENT_MAP.put(SevenElementsEntityTypeTags.DEALS_ANEMO_DAMAGE, Element.ANEMO);
		ElementComponentImpl.ENTITY_TYPE_ELEMENT_MAP.put(SevenElementsEntityTypeTags.DEALS_DENDRO_DAMAGE, Element.DENDRO);
		ElementComponentImpl.ENTITY_TYPE_ELEMENT_MAP.put(SevenElementsEntityTypeTags.DEALS_CRYO_DAMAGE, Element.CRYO);
		ElementComponentImpl.ENTITY_TYPE_ELEMENT_MAP.put(SevenElementsEntityTypeTags.DEALS_GEO_DAMAGE, Element.GEO);

		ElementComponentImpl.DAMAGE_TYPE_ELEMENT_MAP.put(SevenElementsDamageTypeTags.HAS_PYRO_INFUSION, Element.PYRO);
		ElementComponentImpl.DAMAGE_TYPE_ELEMENT_MAP.put(SevenElementsDamageTypeTags.HAS_HYDRO_INFUSION, Element.HYDRO);
		ElementComponentImpl.DAMAGE_TYPE_ELEMENT_MAP.put(SevenElementsDamageTypeTags.HAS_ELECTRO_INFUSION, Element.ELECTRO);
		ElementComponentImpl.DAMAGE_TYPE_ELEMENT_MAP.put(SevenElementsDamageTypeTags.HAS_ANEMO_INFUSION, Element.ANEMO);
		ElementComponentImpl.DAMAGE_TYPE_ELEMENT_MAP.put(SevenElementsDamageTypeTags.HAS_DENDRO_INFUSION, Element.DENDRO);
		ElementComponentImpl.DAMAGE_TYPE_ELEMENT_MAP.put(SevenElementsDamageTypeTags.HAS_CRYO_INFUSION, Element.CRYO);
		ElementComponentImpl.DAMAGE_TYPE_ELEMENT_MAP.put(SevenElementsDamageTypeTags.HAS_GEO_INFUSION, Element.GEO);

		ElementComponent.addElementalInfusionMethod(ElementalInfusionComponent::applyToDamageSource, 1000);
		ElementComponent.addElementalInfusionMethod(ElementComponentImpl::attemptDamageTypeInfusions, 750);
		ElementComponent.addElementalInfusionMethod(ElementComponentImpl::attemptEntityDamageInfusions, 500);
		ElementComponent.addElementalInfusionMethod(ElementComponentImpl::attemptProjectileInfusions, 250);
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

	private static Optional<ElementalDamageSource> attemptProjectileInfusions(DamageSource source, LivingEntity target) {
		// Projectiles are indirect DMG sources.
		if (source.isDirect()) return Optional.empty();

		return source.getSource() instanceof final ProjectileEntity projectile
			? projectile.sevenelements$attemptInfusion(source, target)
			: Optional.empty();
	}
}
