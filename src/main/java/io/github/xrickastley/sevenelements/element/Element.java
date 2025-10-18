package io.github.xrickastley.sevenelements.element;

import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.component.ElementComponentImpl;
import io.github.xrickastley.sevenelements.util.Color;
import io.github.xrickastley.sevenelements.util.Colors;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public enum Element {
	// Only here for Attribute Identification. Other than that, this serves no use, since Physical isn't really an Element.
	PHYSICAL(
		SevenElements.identifier("physical"),
		ElementSettings
			.create()
			.setDamageColor(Color.fromRGBAHex("#ffffff"))
			.canBeAura(false)
	),
	PYRO(
		SevenElements.identifier("pyro"),
		ElementSettings
			.create()
			.setTexture(SevenElements.identifier("textures/element/pyro.png"))
			.setDamageColor(Colors.PYRO)
			.setPriority(2)
			.decayInheritance(false)
			.particleRenderer(new ParticleRenderer(ParticleTypes.FLAME, new Vec3d(0, 0.75, 0), new Vec3d(0.125, 0.250, 0.125), 0.025, 2))
	),
	HYDRO(
		SevenElements.identifier("hydro"),
		ElementSettings
			.create()
			.setTexture(SevenElements.identifier("textures/element/hydro.png"))
			.setDamageColor(Colors.HYDRO)
			.setPriority(2)
			.particleRenderer(new ParticleRenderer(ParticleTypes.FALLING_WATER, new Vec3d(0, 0.75, 0), new Vec3d(0.25, 0.25, 0.25), 0.025, 2))
	),
	ANEMO(
		SevenElements.identifier("anemo"),
		ElementSettings
			.create()
			.setTexture(SevenElements.identifier("textures/element/anemo.png"))
			.setDamageColor(Colors.ANEMO)
			.setPriority(2)
			.canBeAura(false)
	),
	ELECTRO(
		SevenElements.identifier("electro"),
		ElementSettings
			.create()
			.setTexture(SevenElements.identifier("textures/element/electro.png"))
			.setDamageColor(Colors.ELECTRO)
			.setPriority(2)
	),
	DENDRO(
		SevenElements.identifier("dendro"),
		ElementSettings
			.create()
			.setTexture(SevenElements.identifier("textures/element/dendro.png"))
			.setDamageColor(Colors.DENDRO)
			.setPriority(2)
			.setDecayRate(Decays.DENDRO_DECAY_RATE)
			.particleRenderer(new ParticleRenderer(ParticleTypes.TINTED_LEAVES, new Vec3d(0, 0.75, 0), new Vec3d(0.125, 0.250, 0.125), 0.01, 2, "{color: [0, 1, 0, 1]}"))
	),
	CRYO(
		SevenElements.identifier("cryo"),
		ElementSettings
			.create()
			.setTexture(SevenElements.identifier("textures/element/cryo.png"))
			.setDamageColor(Colors.CRYO)
			.setPriority(2)
			.particleRenderer(new ParticleRenderer(ParticleTypes.SNOWFLAKE, new Vec3d(0, 0.75, 0), new Vec3d(0.125, 0.250, 0.125), 0.01, 2))
	),
	GEO(
		SevenElements.identifier("geo"),
		ElementSettings
			.create()
			.setTexture(SevenElements.identifier("textures/element/geo.png"))
			.setDamageColor(Colors.GEO)
			.setPriority(2)
			.canBeAura(false)
	),
	FREEZE(
		SevenElements.identifier("freeze"),
		ElementSettings.create()
			.setTexture(SevenElements.identifier("textures/element/cryo.png"))
			.setDamageColor(Color.fromRGBAHex("#b4ffff"))
			.setPriority(2)
			.bypassesCooldown(true)
			.linkToElement(Element.CRYO)
	),
	BURNING(
		SevenElements.identifier("burning"),
		ElementSettings.create()
			.setTexture(SevenElements.identifier("textures/element/pyro.png"))
			.setDamageColor(Colors.PYRO)
			.setPriority(1)
			.setDecayRate(Decays.NO_DECAY_RATE)
			.bypassesCooldown(true)
			.hasAuraTax(false)
			.linkToElement(Element.PYRO)
	),
	QUICKEN(
		SevenElements.identifier("quicken"),
		ElementSettings.create()
			.setTexture(SevenElements.identifier("textures/element/dendro.png"))
			.setDamageColor(Color.fromRGBAHex("#01e858"))
			.setPriority(2)
			.bypassesCooldown(true)
			.linkToElement(Element.DENDRO)
			.linkGaugeDecayIf(application -> ElementComponent.KEY.get(application.getEntity()).hasElementalApplication(Element.BURNING))
	);

	public static final Codec<Element> CODEC = Codecs.NON_EMPTY_STRING.xmap(Element::valueOf, Element::toString);

	private final Identifier id;
	private final ElementSettings settings;
	private final List<Pair<Element, Predicate<ElementalApplication>>> linkedElements;

	private Element(Identifier id, ElementSettings settings) {
		this.id = id;
		this.settings = settings;
		this.linkedElements = new ArrayList<>();

		if (settings.linkedElement == null) return;

		if (settings.reverseLinkedElement) {
			this.linkedElements.add(new Pair<>(settings.linkedElement, settings.linkDecayOnlyIf));
		} else {
			settings.linkedElement.linkedElements.add(new Pair<>(this, settings.linkDecayOnlyIf));
		}
	}

	public boolean hasDecayInheritance() {
		return settings.decayInheritance;
	}

	public boolean hasTexture() {
		return settings.texture != null;
	}

	public Identifier getTexture() {
		return settings.texture;
	}

	public boolean hasDamageColor() {
		return settings.damageColor != null;
	}

	public Color getDamageColor() {
		return settings.damageColor;
	}

	public Identifier getId() {
		return this.id;
	}

	public boolean canBeAura() {
		return settings.canBeAura;
	}

	public int getPriority() {
		return this.settings.priority;
	}

	public @Nullable Function<ElementalApplication, Number> getCustomDecayRate() {
		return settings.decayRate;
	}

	public boolean bypassesInternalCooldown() {
		return this.settings.bypassesCooldown;
	}

	public boolean hasAuraTax() {
		return settings.hasAuraTax;
	}

	public Text getText() {
		final String string = this.toString();
		final String fallback = string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();

		return Text.translatableWithFallback("seven-elements.element." + string.toLowerCase(), fallback);
	}

	public String getString() {
		return this.getText().getString();
	}

	public void renderEffects(LivingEntity entity) {
		if (this.settings.particleRenderer == null) return;

		this.settings.particleRenderer.render(entity);
	}

	void reduceLinkedElements(double reduction, ElementalApplication application, boolean isGaugeDecay) {
		final ElementComponent component = ElementComponent.KEY.get(application.getEntity());

		if (component == null) return;

		for (final Pair<Element, Predicate<ElementalApplication>> pair : application.getElement().linkedElements) {
			if (!component.hasElementalApplication(pair.getLeft())) continue;

			if (isGaugeDecay && !pair.getRight().test(application)) continue;

			component.getElementalApplication(pair.getLeft()).currentGauge -= reduction;
		}

		ElementComponent.sync(application.getEntity());
	}

	/**
	 * A class used in creating data for Elements, instead of multiple overloaded constructors.
	 */
	private static class ElementSettings {
		private ElementSettings() {}

		private Identifier texture;
		private Color damageColor;
		private int priority;
		private @Nullable Function<ElementalApplication, Number> decayRate = null;
		private boolean canBeAura = true;
		private boolean decayInheritance = true;
		private boolean bypassesCooldown = false;
		private boolean hasAuraTax = true;
		private @Nullable Element linkedElement = null;
		private boolean reverseLinkedElement = false;
		private Predicate<ElementalApplication> linkDecayOnlyIf = entity -> true;
		private @Nullable ParticleRenderer particleRenderer = null;

		/**
		 * Creates a new, empty instance of {@code ElementSettings}.
		 */
		public static ElementSettings create() {
			return new ElementSettings();
		}

		/**
		 * Sets the texture of the element.
		 * @param texture The texture of the element.
		 */
		public ElementSettings setTexture(Identifier texture) {
			this.texture = texture;

			return this;
		}

		/**
		 * Sets the damage color of the element.
		 * @param damageColor The damage color of the element.
		 */
		public ElementSettings setDamageColor(Color damageColor) {
			this.damageColor = damageColor;

			return this;
		}

		/**
		 * Controls the priority of this element over the others. <br> <br>
		 *
		 * An element's <b>priority</b> dictates when it can be applied, reapplied, reacted
		 * with or when it is rendered on top of the entity. Element priority uses natural
		 * ordering, also known as ascending order or "least to greatest". <br> <br>
		 *
		 * For more information, you may refer to the methods that use Element priority.
		 *
		 * @param priority The priority of this element.
		 * @see ElementComponent#getPrioritizedElements() ElementComponent#getPrioritizedElements
		 * @see ElementComponentImpl#triggerReactions(ElementalApplication, net.minecraft.entity.LivingEntity) ElementComponentImpl#triggerReactions
		 * @see ElementComponentImpl#attemptReapply(ElementalApplication) ElementComponentImpl#attemptReapply
		 */
		public ElementSettings setPriority(int priority) {
			this.priority = priority;

			return this;
		}

		/**
		 * Sets the function controlling the decay rate of this element. <br> <br>
		 *
		 * This function must output a number {@code x} such that {@code x} is the amount of Gauge
		 * Units deducted per tick.
		 *
		 * @param decayRate The damage color of the element.
		 */
		public ElementSettings setDecayRate(@NotNull Function<ElementalApplication, Number> decayRate) {
			this.decayRate = decayRate;

			return this;
		}

		/**
		 * Sets if the element can be an Aura Element.
		 * @param aura If the element can be an Aura Element.
		 */
		public ElementSettings canBeAura(boolean aura) {
			this.canBeAura = aura;

			return this;
		}

		/**
		 * Sets if the element, when applied as an Aura Element, would have its Gauge Units
		 * deducted by the <a href="https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory#Aura_Tax">Aura Tax</a>.
		 * @param auraTax If the element's Gauge Units should be deducted by the Aura Tax.
		 */
		public ElementSettings hasAuraTax(boolean auraTax) {
			this.hasAuraTax = auraTax;

			return this;
		}

		/**
		 * Sets if the Elemental Application tied to this element is tied to can bypass <a href="https://genshin-impact.fandom.com/wiki/Internal_Cooldown">Internal Cooldown</a>.
		 * @param bypassesCooldown If the Elemental Application tied to this element can bypass Internal Cooldown.
		 */
		public ElementSettings bypassesCooldown(boolean bypassesCooldown) {
			this.bypassesCooldown = bypassesCooldown;

			return this;
		}

		/**
		 * Sets if the element has <a href="https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory#Decay_Rate_Inheritance">decay rate inheritance</a>.
		 * @param decayInheritance If the element has decay rate inheritance.
		 */
		public ElementSettings decayInheritance(boolean decayInheritance) {
			this.decayInheritance = decayInheritance;

			return this;
		}

		/**
		 * Sets the particle renderer for this Element. <br> <br>
		 *
		 * This renders particles on the entity per tick while the element is applied.
		 *
		 * @param particleRenderer The particle renderer to use.
		 */
		public ElementSettings particleRenderer(ParticleRenderer particleRenderer) {
			this.particleRenderer = particleRenderer;

			return this;
		}

		/**
		 * Links this element to the provided {@code element}. <br> <br>
		 *
		 * Upon linking, <i>gauge reduction</i> not originating from the gauge decay will be
		 * "synced" to the gauge units of this element, if it exists. <br> <br>
		 *
		 * Do note that linking isn't a "recursive" operation, i.e. Element A linked to Element B,
		 * Element B linked to Element C, reduction on Element A, B and C. <br> <br>
		 *
		 * You may also choose to sync the gauge decay permanently or with a {@code Predicate}.
		 *
		 * @param element The {@link Element} to link this element to.
		 * @see ElementSettings#linkElement(Element) ElementSettings#linkElement — For linking the specified {@code element} to <b>this</b> element.
		 * @see ElementSettings#linkGaugeDecay(boolean) ElementSettings#linkGaugeDecay — For permanent gauge decay linking.
		 * @see ElementSettings#linkGaugeDecayIf(Predicate) ElementSettings#linkGaugeDecayIf — For conditional gauge decay linking.
		 */
		public ElementSettings linkToElement(Element element) {
			this.linkedElement = element;

			return this;
		}

		/**
		 * Links the provided {@code element} to this element. <br> <br>
		 *
		 * Upon linking, <i>gauge reduction</i> not originating from the gauge decay will be
		 * "synced" to the gauge units of the provided {@code element}, if it exists. <br> <br>
		 *
		 * Do note that linking isn't a "recursive" operation, i.e. Element A linked to Element B,
		 * Element B linked to Element C, reduction on Element A, B and C. <br> <br>
		 *
		 * You may also choose to sync the gauge decay permanently or with a {@code Predicate}.
		 *
		 * @param element The {@link Element} to link to this element.
		 * @see ElementSettings#linkToElement(Element) ElementSettings#linkToElement — For linking <b>this</b> element to the specified {@code element}.
		 * @see ElementSettings#linkGaugeDecay(boolean) ElementSettings#linkGaugeDecay — For permanent gauge decay linking.
		 * @see ElementSettings#linkGaugeDecayIf(Predicate) ElementSettings#linkGaugeDecayIf — For conditional gauge decay linking.
		 */
		@SuppressWarnings("unused")
		public ElementSettings linkElement(Element element) {
			this.linkedElement = element;
			this.reverseLinkedElement = true;

			return this;
		}

		/**
		 * Sets whether the gauge decay is linked to the gauge of this element, or the gauge of the
		 * corresponding element, if {@code reverse} was {@code true} for
		 * {@link ElementSettings#linkElement(Element, boolean) ElementSettings#linkedElement}.
		 *
		 * @param link Whether the gauge decay is also linked.
		 */
		@SuppressWarnings("unused")
		public ElementSettings linkGaugeDecay(boolean link) {
			return this.linkGaugeDecayIf(a -> link);
		}

		/**
		 * Sets whether, at this instance in time, the gauge decay is linked to the gauge of this
		 * element, or the gauge of the corresponding element, if {@code reverse} was {@code true}
		 * for {@link ElementSettings#linkElement(Element, boolean) ElementSettings#linkedElement}.
		 *
		 * @param predicate A {@code Predicate} indicating whether the gauge decay is also linked at this instance in time. The passed {@code ElementalApplication} is the one belonging to the linked element, or more formally, the element this element is linked to.
		 */
		public ElementSettings linkGaugeDecayIf(Predicate<ElementalApplication> predicate) {
			this.linkDecayOnlyIf = predicate;

			return this;
		}
	}

	private static class Decays {
		private static final Function<ElementalApplication, Number> NO_DECAY_RATE = a -> {
			return 0;
		};

		private static final Function<ElementalApplication, Number> DENDRO_DECAY_RATE = application -> {
			final ElementComponent component = ElementComponent.KEY.get(application.getEntity());

			return component.hasElementalApplication(Element.valueOf("BURNING"))
				// max(0.4, Natural Decay Rate_Dendro Aura × 2)
				// 0.4 is in GU/s, convert to GU/tick
				? Math.max(0.02, application.getDefaultDecayRate() * 2)
				: application.getDefaultDecayRate();
		};
	}

	private static record ParticleRenderer(ParticleType<? extends ParticleEffect> particle, Vec3d relativePos, Vec3d delta, double speed, int count, NbtCompound compound) {
		private static final Random random = Random.create();

		ParticleRenderer(ParticleType<? extends ParticleEffect> particle, Vec3d relativePos, Vec3d delta, double speed, int count, String nbt) {
			this(particle, relativePos, delta, speed, count, parseCompound(nbt));
		}

		ParticleRenderer(ParticleType<? extends ParticleEffect> particle, Vec3d relativePos, Vec3d delta, double speed, int count) {
			this(particle, relativePos, delta, speed, count, new NbtCompound());
		}

		private static NbtCompound parseCompound(String nbt) {
			try {
				return StringNbtReader.readCompound(nbt);
			} catch (Exception e) {
				RuntimeException e2 = new NbtException("An invalid NBT string was provided!");
				e2.addSuppressed(e);

				throw e2;
			}
		}

		private ParticleEffect getParticle(World world) {
			return particle
				.getCodec()
				.codec()
				.parse(world.getRegistryManager().getOps(NbtOps.INSTANCE), compound)
				.getOrThrow();
		}

		private void render(LivingEntity entity) {
			// ServerWorld has no impl. for addParticle.
			if (!entity.getEntityWorld().isClient()) return;

			final Box box = entity.getBoundingBox();
			final Vec3d pos = entity.getEntityPos().add(relativePos.multiply(box.getLengthX(), box.getLengthY(), box.getLengthZ()));

			if (count == 0) this.addSingleParticle(entity, pos);
			else this.addMultipleParticles(entity, pos);
		}

		private void addSingleParticle(LivingEntity entity, Vec3d pos) {
			final World world = entity.getEntityWorld();

			double velX = speed * delta.x;
			double velY = speed * delta.y;
			double velZ = speed * delta.z;

			world.addParticleClient(this.getParticle(world), pos.x, pos.y, pos.z, velX, velY, velZ);
		}

		private void addMultipleParticles(LivingEntity entity, Vec3d pos) {
			final World world = entity.getEntityWorld();

			for (int i = 0; i < count; ++i) {
				double randX = random.nextGaussian() * delta.x;
				double randY = random.nextGaussian() * delta.y;
				double randZ = random.nextGaussian() * delta.z;
				double velX = random.nextGaussian() * speed;
				double velY = random.nextGaussian() * speed;
				double velZ = random.nextGaussian() * speed;

				world.addParticleClient(this.getParticle(world), pos.x + randX, pos.y + randY, pos.z + randZ, velX, velY, velZ);
			}
		}
	}
}
