package io.github.xrickastley.sevenelements.element.reaction;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication.Type;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.events.ReactionTriggered;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.networking.ShowElementalReactionS2CPayload;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistries;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class ElementalReaction {
	protected final String name;
	protected final Identifier id;
	protected final @Nullable Text text;
	protected final double reactionCoefficient;
	protected final Pair<Element, Integer> auraElement;
	protected final Pair<Element, Integer> triggeringElement;
	protected final boolean reversable;
	protected final boolean applyResultAsAura;
	protected final boolean endsReactionTrigger;
	protected final boolean preventsPriorityUpgrade;
	protected final Set<Identifier> preventsReactionsAfter;
	protected final List<Element> reactionDisplayOrder;

	protected ElementalReaction(Settings settings) {
		this.name = settings.name;
		this.id = settings.id;
		this.text = settings.text;

		this.reactionCoefficient = settings.reactionCoefficient;
		this.auraElement = settings.auraElement;
		this.triggeringElement = settings.triggeringElement;
		this.reversable = settings.reversable;
		this.applyResultAsAura = settings.applyResultAsAura;
		this.endsReactionTrigger = settings.endsReactionTrigger;
		this.preventsPriorityUpgrade = settings.preventsPriorityUpgrade;
		this.preventsReactionsAfter = settings.preventsReactionsAfter;

		final Stream<Element> reactionDisplayOrder = settings.reactionDisplayOrder.isEmpty()
			? Stream.of(ClassInstanceUtil.mapOrNull(settings.auraElement, Pair::getLeft), ClassInstanceUtil.mapOrNull(settings.triggeringElement, Pair::getLeft))
			: settings.reactionDisplayOrder.stream();

		this.reactionDisplayOrder = reactionDisplayOrder
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		SevenElementsRegistries.ELEMENTAL_REACTION.createEntry(this);
	}

	public static float getReactionDamage(Entity entity, double reactionMultiplier) {
		return ElementalReaction.getReactionDamage(entity, (float) reactionMultiplier);
	}

	public static float getReactionDamage(Entity entity, float reactionMultiplier) {
		return SevenElements.getLevelMultiplier(entity) * reactionMultiplier;
	}

	public static float getReactionDamage(World world, double reactionMultiplier) {
		return ElementalReaction.getReactionDamage(world, (float) reactionMultiplier);
	}

	public static float getReactionDamage(World world, float reactionMultiplier) {
		return SevenElements.getLevelMultiplier(world) * reactionMultiplier;
	}

	public static List<LivingEntity> getEntitiesInAoE(LivingEntity target, double radius) {
		return getEntitiesInAoE(target, radius, e -> true);
	}

	public static List<LivingEntity> getEntitiesInAoE(LivingEntity target, double radius, Predicate<LivingEntity> filter) {
		final List<LivingEntity> targets = target
			.getWorld()
			.getNonSpectatingEntities(LivingEntity.class, Box.of(target.getLerpedPos(1f), radius * 2, radius * 2, radius * 2));

		targets.removeIf(entity -> entity.squaredDistanceTo(target) >= radius * radius || filter.negate().test(entity));

		return targets;
	}

	public boolean hasElement(Element element) {
		return element == this.auraElement.getLeft() || element == this.triggeringElement.getLeft();
	}

	public boolean hasAnyElement(Collection<Element> elements) {
		return this.hasAnyElement(elements.stream());
	}

	public boolean hasAnyElement(Stream<Element> elements) {
		return elements.anyMatch(this::hasElement);
	}

	public Element getAuraElement() {
		return auraElement.getLeft();
	}

	public Element getTriggeringElement() {
		return triggeringElement.getLeft();
	}

	public int getAuraElementPriority() {
		return auraElement.getRight();
	}

	public int getTriggeringElementPriority() {
		return triggeringElement.getRight();
	}

	public int getHighestElementPriority() {
		return Math.min(this.auraElement.getLeft().getPriority(), this.triggeringElement.getLeft().getPriority());
	}

	public @Nullable Text getText() {
		return text;
	}

	public Pair<Element, Integer> getElementPair(Element element) {
		return element == auraElement.getLeft()
			? auraElement
			: element == triggeringElement.getLeft()
				? triggeringElement
				: null;
	}

	public List<Element> getReactionDisplayOrder() {
		return this.reactionDisplayOrder;
	}

	public boolean shouldApplyResultAsAura() {
		return this.applyResultAsAura;
	}

	public boolean shouldEndReactionTrigger() {
		return this.endsReactionTrigger;
	}

	public boolean shouldPreventPriorityUpgrade() {
		return this.preventsPriorityUpgrade;
	}

	public boolean preventsReaction(ElementalReaction reaction) {
		return this.preventsReaction(reaction.getId());
	}

	public boolean preventsReaction(Identifier reactionId) {
		return this.preventsReactionsAfter.contains(reactionId);
	}

	/**
	 * Gets the priority of this Elemental Reaction.
	 * @param application The applied Elemental Application, which should be the triggering element.
	 * @return The priority of this Elemental Reaction.
	 */
	public int getPriority(ElementalApplication application) {
		return getPriority(application.getElement());
	}

	/**
	 * Gets the priority of this Elemental Reaction.
	 * @param triggeringElement The applied element, also known as the triggering element.
	 * @return The priority of this Elemental Reaction.
	 */
	public int getPriority(Element triggeringElement) {
		return triggeringElement.equals(this.triggeringElement.getLeft())
			? this.triggeringElement.getRight()
			: triggeringElement.equals(this.auraElement.getLeft()) && this.reversable
				? this.auraElement.getRight()
				:  Integer.MAX_VALUE;
	}

	/**
	 * The function to execute after the Elemental Reaction has been triggered. This function is executed after both elements have reacted and have been reduced.
	 * @param entity The {@code LivingEntity} this Elemental Reaction was triggered on.
	 * @param auraElement The aura element that triggered this reaction.
	 * @param triggeringElement The triggering element that reacted with the aura element.
	 * @param reducedGauge The gauge units reduced from both Elements. This will always be {@code Math.min(auraElementGU, triggeringElementGU * reactionCoefficient)}
	 * @param origin The {@code LivingEntity} that triggered this Elemental Reaction.
	 */
	protected abstract void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin);

	public Identifier getId() {
		return id;
	}

	public boolean isTriggerable(Entity entity) {
		return entity instanceof final LivingEntity livingEntity && isTriggerable(livingEntity);
	}

	public boolean isTriggerable(LivingEntity entity) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		final ElementalApplication auraElement = component.getElementalApplication(this.auraElement.getLeft());
		final ElementalApplication trigElement = component.getElementalApplication(this.triggeringElement.getLeft());

		return reversable
			// Any of the elements can be an Aura element.
			? auraElement != null && trigElement != null && !auraElement.isEmpty() && !trigElement.isEmpty()
			// The aura element must STRICTLY be an Aura element.
			: auraElement != null && trigElement != null && auraElement.isAuraElement() && !auraElement.isEmpty() && !trigElement.isEmpty();
	}

	public boolean trigger(LivingEntity entity) {
		return this.trigger(entity, null);
	}

	public boolean trigger(LivingEntity entity, @Nullable LivingEntity origin) {
		if (!isTriggerable(entity)) return false;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		ElementalApplication applicationAE = component.getElementalApplication(auraElement.getLeft());
		ElementalApplication applicationTE = component.getElementalApplication(triggeringElement.getLeft());

		if (applicationTE.isAuraElement() && !applicationAE.isAuraElement()) {
			ElementalApplication a = applicationTE;
			applicationTE = applicationAE;
			applicationAE = a;
		}

		final double reducedGauge = applicationAE.reduceGauge(reactionCoefficient * applicationTE.getCurrentGauge());
		applicationTE.reduceGauge(reducedGauge);

		this.onTrigger(entity, applicationAE, applicationTE, reducedGauge, origin);

		return true;
	}

	protected final void onTrigger(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		this.onReaction(entity, auraElement, triggeringElement, reducedGauge, origin);
		this.displayReaction(entity);

		ReactionTriggered.EVENT
			.invoker()
			.onReactionTriggered(this, reducedGauge, entity, origin);

		entity
			.getWorld()
			.playSound(null, entity.getBlockPos(), SevenElementsSoundEvents.REACTION, SoundCategory.PLAYERS, 1.0f, 1.0f);
	}

	public boolean idEquals(ElementalReaction reaction) {
		return this.getId().equals(reaction.getId());
	}

	protected void displayReaction(LivingEntity target) {
		if (target.getWorld().isClient) return;

		final Box boundingBox = target.getBoundingBox();

		final double x = target.getX() + (boundingBox.getXLength() * 1.50 * Math.random());
		final double y = target.getY() + (boundingBox.getYLength() * (0.25 + (Math.random() / 2.0)));
		final double z = target.getZ() + (boundingBox.getZLength() * 1.50 * Math.random());

		final Vec3d pos = new Vec3d(x, y, z);

		final ShowElementalReactionS2CPayload packet = new ShowElementalReactionS2CPayload(pos, this);

		if (target instanceof final ServerPlayerEntity serverPlayer) ServerPlayNetworking.send(serverPlayer, packet);

		for (final ServerPlayerEntity otherPlayer : PlayerLookup.tracking(target)) {
			if (otherPlayer.getId() == target.getId()) continue;

			ServerPlayNetworking.send(otherPlayer, packet);
		}
	}

	public static final class Settings {
		private final String name;
		private final Identifier id;
		private final @Nullable Text text;
		private double reactionCoefficient = 1.0;
		private Pair<Element, Integer> auraElement;
		private Pair<Element, Integer> triggeringElement;
		private boolean reversable = false;
		private boolean applyResultAsAura = false;
		private boolean endsReactionTrigger = false;
		private boolean preventsPriorityUpgrade = false;
		private Set<Identifier> preventsReactionsAfter = new HashSet<>();
		private List<Element> reactionDisplayOrder = new ArrayList<>();

		public Settings(String name, Identifier id, @Nullable Text text) {
			this.name = name;
			this.id = id;
			this.text = text;
		}

		/**
		 * Sets the reaction coefficient of the Elemental Reaction. This is a multiplier that dictates how many gauge units are
		 * consumed from the aura element.
		 * @param reactionCoefficient The reaction coefficient of the Elemental Reaction.
		 */
		public Settings setReactionCoefficient(double reactionCoefficient) {
			this.reactionCoefficient = reactionCoefficient;

			return this;
		}

		public Settings setAuraElement(Element element) {
			return setAuraElement(element, -1);
		}

		/**
		 * Sets the Aura Element of the Elemental Reaction. <br> <br>
		 *
		 * This is the element that <b>must</b> be applied onto the entity in order for the reaction to
		 * be triggered by applying the Triggering Element onto the entity. <br> <br>
		 *
		 * However, when the Elemental Reaction is considered "reversable" through
		 * {@link Settings#reversable(boolean) Settings#reversable()},
		 * the Aura Element may be considered as the Triggering Element, and the Triggering Element
		 * may be considered as the Aura Element.
		 *
		 * @param element The Aura Element of the Elemental Reaction.
		 * @param priority The priority of this reaction triggering when {@code auraElement} is currently
		 * the triggering element. {@code priority} will only be applied when {@code reversable} is
		 * {@code true}, as that is the only instance the {@code auraElement} can be considered a
		 * triggering element.
		 */
		public Settings setAuraElement(Element element, int priority) {
			this.auraElement = new Pair<>(element, priority);

			return this;
		}

		public Settings setTriggeringElement(Element element) {
			return setTriggeringElement(element, -1);
		}

		/**
		 * Sets the Triggering Element of the Elemental Reaction. <br> <br>
		 *
		 * This is the element that <b>must</b> be applied onto the entity with the specified Aura
		 * Element in order for the reaction to be triggered. <br> <br>
		 *
		 * However, when the Elemental Reaction is considered "reversable" through
		 * {@link Settings#reversable(boolean) Settings#reversable()},
		 * the Triggering Element may be considered as the Aura Element, and the Aura Element may be
		 * considered as the Triggering Element.
		 *
		 * @param element The Triggering Element of the Elemental Reaction.
		 * @param priority The priority of this reaction triggering when {@code triggeringElement} is the triggering element.
		 */
		public Settings setTriggeringElement(Element element, int priority) {
			this.triggeringElement = new Pair<>(element, priority);

			return this;
		}

		/**
		 * Sets the display order when this reaction is displayed. <br> <br>
		 *
		 * This <b>must</b> contain <b>only</b> the Aura and Triggering element, as an
		 * {@code IllegalArgumentException} will be thrown when a different unexpected {@code Element}
		 * is added instead.
		 *
		 * @param elementOrder The order of the elements when this reaction is displayed.
		 */
		public Settings setReactionDisplayOrder(Element... elementOrder) {
			this.reactionDisplayOrder = List.of(elementOrder);

			final Set<Element> onlyElements = Set.of(this.auraElement.getLeft(), this.triggeringElement.getLeft());
			final List<Element> invalidElements = this.reactionDisplayOrder
				.stream()
				.filter(Predicate.not(onlyElements::contains))
				.toList();

			if (this.reactionDisplayOrder.stream().anyMatch(Predicate.not(onlyElements::contains)))
				throw new IllegalArgumentException("The elements: " + invalidElements + " are not permitted as part of the reaction display order!");

			return this;
		}

		/**
		 * Sets the Elemental Reaction as reversable. <br> <br>
		 *
		 * When this is {@code true}, the <b>Triggering Element</b> can be considered as an <b>Aura
		 * Element</b>.
		 *
		 * @param reversable Whether the Elemental Reaction is reversable.
		 */
		public Settings reversable(boolean reversable) {
			this.reversable = reversable;

			return this;
		}

		/**
		 * Whether the triggering Element is applied as an aura. <br> <br>
		 *
		 * Once all possible Elemental Reactions have been triggered, the triggering element
		 * may have some Gauge Units left. This setting allows for the remaining Gauge Units
		 * to be applied as an Elemental Aura. <br> <br>
		 *
		 * If multiple Elemental Reactions are triggered, all triggered Elemental Reactions
		 * must have this setting set to {@code true} for the Gauge Units from the triggering
		 * element to be applied as an Elemental Aura. <br> <br>
		 *
		 * Do note that this setting will not affect Elemental Applications with {@link Type#DURATION},
		 * as those are always applied as an Aura Element after possible reactions.
		 *
		 * @param applyResultAsAura Whether the remaining Gauge Units from the triggering
		 * element are applied as an Elemental Aura.
		 */
		public Settings applyResultAsAura(boolean applyResultAsAura) {
			this.applyResultAsAura = applyResultAsAura;

			return this;
		}

		/**
		 * Whether this reaction ends all future reactions from triggering. <br> <br>
		 *
		 * Once a reaction is triggered, an attempt to trigger another is made. This setting denies
		 * other reactions to be triggered after triggering this reaction.
		 *
		 * @param endsReactionTrigger Whether reactions can be triggered after this reaction.
		 */
		public Settings endsReactionTrigger(boolean endsReactionTrigger) {
			this.endsReactionTrigger = endsReactionTrigger;

			return this;
		}

		/**
		 * Whether this reaction prevents the priority upgrade. <br> <br>
		 *
		 * Once a reaction is triggered, an attempt to trigger another is made. If no reactions were
		 * found, an attempt to upgrade the "element priority" is done. This setting denies that
		 * attempt after triggering this reaction. <br> <br>
		 *
		 * However, the attempt to upgrade the priority will only be denied <b>once</b> after this
		 * reaction. Succeeding reactions <b>must</b> also have this property enabled in order for the
		 * upgrade to be <i>fully</i> denied.
		 *
		 * @param preventsPriorityUpgrade Whether the element priority can be upgraded after
		 * this reaction.
		 */
		public Settings preventsPriorityUpgrade(boolean preventsPriorityUpgrade) {
			this.preventsPriorityUpgrade = preventsPriorityUpgrade;

			return this;
		}

		/**
		 * Whether this reaction prevents other reactions from triggering after it. <br> <br>
		 *
		 * This setting denies the specified reactions from triggering <b>directly after</b> this
		 * reaction.
		 *
		 * @param reactions The reactions to prevent from triggering <b>directly after</b> this reaction.
		 */
		public Settings preventsReactionsAfter(Identifier ...reactions) {
			this.preventsReactionsAfter = Set.of(reactions);

			return this;
		}

		public Element getAuraElement() {
			return auraElement.getLeft();
		}

		public Element getTriggeringElement() {
			return triggeringElement.getLeft();
		}
	}
}
