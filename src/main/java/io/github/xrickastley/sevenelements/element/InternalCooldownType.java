package io.github.xrickastley.sevenelements.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistryKeys;
import io.github.xrickastley.sevenelements.registry.dynamic.SevenElementsRegistryLoader;

import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

/**
 * An {@code InternalCooldownType} is a class used for holding different types of Internal Cooldowns
 * by having various instances of the "Reset Interval" and the "Gauge Sequence" variables. <br> <br>
 *
 * <h2>Parameters</h2>
 *
 * The <b>id</b> is the unique {@code Identifier} that seperates this {@code InternalCooldownType} instance
 * from the rest. Regardless of the <b>reset interval</b> or <b>gauge sequence</b>'s values; if the value
 * for the {@code id} is different, that {@code InternalCooldownType} instance is regarded to be different
 * from one that has the same values. <br> <br>
 *
 * The <b>reset interval</b> is the amount of ticks required before the timer resets. After the timer
 * has reset, the <b>gauge sequence</b> is set back to {@code 0} and the Internal Cooldown is considered to
 * <b>not</b> be active. <br> <br>
 *
 * The <b>gauge sequence</b> is the amount of elemental attacks required before an element can be
 * applied again, where the elemental attacks share ICD. When the amount of elemental attacks
 * exceed that of the gauge sequence, the Internal Cooldown is considered to <b>not</b> be active,
 * <b>however</b>, this does <b>not</b> reset the timer given by the <b>reset interval</b>.
 *
 * <h2>Hardcoded ICD Types</h2>
 *
 * An {@code InternalCooldownType} isn't validated or registered against the registry, so a "hardcoded"
 * {@code InternalCooldownType} instance may exist. However, due to the fact that these instances aren't
 * registered against the registry, there may be instances where two seperate {@code InternalCooldownType}
 * instances have the same {@code id}. <br> <br>
 *
 * Though harmless in practice, it may be confusing to have two {@code InternalCooldownType} instances at
 * the same time. The {@link InternalCooldownType} class provides two methods you may use to create
 * hardcoded instances of it.
 *
 * <ul>
 * 	<li>{@link InternalCooldownType#of(Identifier, int, int) InternalCooldownType.of()} creates a <i>purely</i> hardcoded instance of {@code InternalCooldownType}.</li>
 * 	<li>{@link InternalCooldownType#registered(Identifier, int, int) InternalCooldownType.registered()} creates a hardcoded instance of {@code InternalCooldownType} that is
 * 	registered to the dynamic Internal Cooldown Type registry. This hardcoded instance is <i>not</i>
 * 	overwritable by data-driven means.</li>
 * </ul>
 */
public final class InternalCooldownType {
	private static final List<InternalCooldownType> PRELOADED_INSTANCES = new ArrayList<>();
	public static final InternalCooldownType NONE = InternalCooldownType.registered(SevenElements.identifier("none"), 0, 0);
	public static final InternalCooldownType DEFAULT = InternalCooldownType.registered(SevenElements.identifier("default"), 50, 3);
	public static final InternalCooldownType INTERVAL_ONLY = InternalCooldownType.registered(SevenElements.identifier("interval_only"), 50, Integer.MAX_VALUE);

	public static final Codec<InternalCooldownType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Identifier.CODEC.fieldOf("id").forGetter(InternalCooldownType::getId),
		Codecs.rangedInt(0, Integer.MAX_VALUE).optionalFieldOf("reset_interval", 50).forGetter(InternalCooldownType::getResetInterval),
		Codecs.rangedInt(0, Integer.MAX_VALUE).optionalFieldOf("gauge_sequence", 3).forGetter(InternalCooldownType::getGaugeSequence)
	).apply(instance, InternalCooldownType::new));

	public static final RegistryElementCodec<InternalCooldownType> REGISTRY_CODEC = RegistryElementCodec.of(SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE, InternalCooldownType.CODEC);

	private final Identifier id;
	private final int resetInterval;
	private final int gaugeSequence;

	private InternalCooldownType(Identifier id, int resetInterval, int gaugeSequence) {
		if (resetInterval < 0) throw new IllegalArgumentException("Provided resetInterval " + resetInterval + " is out of range: [0, " + Integer.MAX_VALUE +"]");

		if (gaugeSequence < 0) throw new IllegalArgumentException("Provided gaugeSequence " + gaugeSequence + " is out of range: [0, " + Integer.MAX_VALUE +"]");

		this.id = id;
		this.resetInterval = resetInterval;
		this.gaugeSequence = gaugeSequence;
	}

	public static InternalCooldownType of(Identifier id, int resetInterval, int gaugeSequence) {
		return new InternalCooldownType(id, resetInterval, gaugeSequence);
	}

	public static InternalCooldownType registered(Identifier id, int resetInterval, int gaugeSequence) {
		final InternalCooldownType icdType = new InternalCooldownType(id, resetInterval, gaugeSequence);

		InternalCooldownType.PRELOADED_INSTANCES.add(icdType);

		return icdType;
	}

	public static void onBeforeRegistryLoad(Registry<InternalCooldownType> registry) {
		InternalCooldownType.PRELOADED_INSTANCES.forEach(inst -> Registry.register(registry, inst.getId(), inst));
	}

	public static List<Identifier> getPreloadedInstances() {
		return InternalCooldownType.PRELOADED_INSTANCES
			.stream()
			.map(InternalCooldownType::getId)
			.toList();
	}

	public Identifier getId() {
		return id;
	}

	public int getResetInterval() {
		return resetInterval;
	}

	public int getGaugeSequence() {
		return gaugeSequence;
	}

	public Builder getBuilder() {
		return new Builder(resetInterval, gaugeSequence);
	}

	@Override
	public String toString() {
		return String.format("InternalCooldownType[%s/resetInterval=%d,gaugeSequence=%d]", this.id, this.resetInterval, this.gaugeSequence);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (!(obj instanceof final InternalCooldownType type)) return false;

		return this.id.equals(type.id)
			&& this.gaugeSequence == type.gaugeSequence
			&& this.resetInterval == type.resetInterval;
	}

	public MutableText getText() {
		return Text.literal(this.id.toString());
	}

	public MutableText getText(boolean addValues) {
		if (!addValues) return this.getText();

		final String gaugeSequence = this.gaugeSequence == Integer.MAX_VALUE
			? DecimalFormatSymbols.getInstance().getInfinity()
			: String.valueOf(this.gaugeSequence); // no need for -inf, gauge sequence cannot and should not be less than 0.

		return Text.empty()
			.append(this.getText())
			.append(" (")
			.append(Text.translatable("formats.seven-elements.icd_type", this.resetInterval / 20.0, gaugeSequence))
			.append(")");
	}

	/**
	 * An intermediary class between the {@code InternalCooldownType} and its serialized version. <br> <br>
	 *
	 * On deserializing datapack entries, the Codec of this class is used, and is transformed into
	 * an {@code InternalCooldownType} with the {@code Identifier} of the data pack entry using the
	 * {@link Builder#getInstance(Identifier) Builder#getInstance} method. <br> <br>
	 *
	 * This is done through the
	 * {@link io.github.xrickastley.sevenelements.registry.dynamic.DynamicRegistries#registerIdentified(Class, net.minecraft.registry.RegistryKey, Codec, Codec, java.util.function.BiFunction) DynamicRegistries#registerIdentified}
	 * method via the
	 * {@link SevenElementsRegistryLoader SevenElementsRegistryLoader}
	 * class.
	 */
	public static final class Builder {
		public static final Codec<InternalCooldownType.Builder> CODEC = RecordCodecBuilder.create(instance ->
			instance
				.group(
					Codec
						.intRange(0, Integer.MAX_VALUE)
						.optionalFieldOf("reset_interval", 50)
						.forGetter(Builder::getResetInterval),
					Codec
						.intRange(0, Integer.MAX_VALUE)
						.optionalFieldOf("gauge_sequence", 3)
						.forGetter(Builder::getGaugeSequence)
				)
				.apply(instance, Builder::new)
		);

		private final int resetInterval;
		private final int gaugeSequence;

		private Builder(int resetInterval, int gaugeSequence) {
			this.resetInterval = resetInterval;
			this.gaugeSequence = gaugeSequence;
		}

		public InternalCooldownType getInstance(Identifier registryId) {
			return new InternalCooldownType(registryId, resetInterval, gaugeSequence);
		}

		private int getResetInterval() {
			return resetInterval;
		}

		private int getGaugeSequence() {
			return gaugeSequence;
		}
	}
}
