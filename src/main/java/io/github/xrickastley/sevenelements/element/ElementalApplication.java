package io.github.xrickastley.sevenelements.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.reaction.AbstractBurningElementalReaction;
import io.github.xrickastley.sevenelements.exception.ElementalApplicationOperationException.Operation;
import io.github.xrickastley.sevenelements.exception.ElementalApplicationOperationException;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;

/**
 * A class representing an Elemental Application for an entity.
 */
public abstract sealed class ElementalApplication permits DurationElementalApplication, GaugeUnitElementalApplication {
	protected static final DecimalFormat GAUGE_UNIT_FORMAT = new DecimalFormat("0.0");
	protected static final DecimalFormat DURATION_FORMAT = new DecimalFormat("0.00");

	protected final Type type;
	protected final Element element;
	protected final LivingEntity entity;
	protected final boolean isAura;
	// Used in uniquely identifying Elemental Applications.
	protected final UUID uuid;
	protected double gaugeUnits;
	protected double currentGauge;
	protected long appliedAt;

	ElementalApplication(Type type, LivingEntity entity, Element element, UUID uuid, double gaugeUnits, boolean isAura) {
		this.type = type;
		this.entity = entity;
		this.element = element;
		this.isAura = isAura;
		this.uuid = uuid;

		this.gaugeUnits = gaugeUnits;
		this.currentGauge = gaugeUnits;
		this.appliedAt = entity.getEntityWorld().getTime();
	}

	/**
	 * Gets the Type of this Elemental Application.
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * Gets the {@code Element} of to this Elemental Application.
	 */
	public Element getElement() {
		return this.element;
	}

	public LivingEntity getEntity() {
		return this.entity;
	}

	public UUID getUuid() {
		return this.uuid;
	}

	public double getGaugeUnits() {
		return this.gaugeUnits;
	}

	public double getCurrentGauge() {
		return this.currentGauge;
	}

	public long getAppliedAt() {
		return this.appliedAt;
	}

	/**
	 * Gets the number of ticks this Elemental Application has been applied for.
	 */
	public long getAppliedTicks() {
		return this.entity.getEntityWorld().getTime() - this.appliedAt;
	}

	/**
	 * Gets the default decay rate of this Elemental Application.
	 * @return The default decay rate of this Elemental Application in {@code GU/tick}.
	 */
	protected abstract double getDefaultDecayRate();

	public abstract int getRemainingTicks();

	public Text getText() {
		return this.getText(GAUGE_UNIT_FORMAT, DURATION_FORMAT);
	}

	public Text getText(@Nullable String gaugeFormat) {
		return this.getText(
			ClassInstanceUtil.mapOrNull(gaugeFormat, DecimalFormat::new),
			DURATION_FORMAT
		);
	}

	public Text getText(@Nullable DecimalFormat gaugeFormat) {
		return this.getText(gaugeFormat, DURATION_FORMAT);
	}

	public Text getText(@Nullable String gaugeFormat, @Nullable String durationFormat) {
		return this.getText(
			ClassInstanceUtil.<String, DecimalFormat>mapOrNull(gaugeFormat, DecimalFormat::new),
			ClassInstanceUtil.<String, DecimalFormat>mapOrNull(durationFormat, DecimalFormat::new)
		);
	}

	public abstract Text getText(@Nullable DecimalFormat gaugeFormat, @Nullable DecimalFormat durationFormat);

	/**
	 * Returns whether this Elemental Application is using Gauge Units.
	 */
	public boolean isGaugeUnits() {
		return this.type == Type.GAUGE_UNIT;
	}

	/**
	 * Returns whether this Elemental Application is using a specified duration.
	 */
	public boolean isDuration() {
		return this.type == Type.DURATION;
	}

	/**
	 * Checks if the element in this Elemental Application is of the given {@code element}.
	 * @param element The {@code Element} to compare with this Elemental Application.
	 */
	public boolean isOfElement(Element element) {
		return this.element == element;
	}

	/**
	 * Returns whether this Elemental Application is an aura element.
	 */
	public boolean isAuraElement() {
		return this.isAura;
	}

	/**
	 * Returns whether this Elemental Application is empty.
	 */
	public abstract boolean isEmpty();

	/**
	 * Reduces the amount of gauge units in this Elemental Application, then returns the eventual amount of gauge units reduced.
	 * @param gaugeUnits The amount of gauge units to reduce.
	 * @return The eventual amount of gauge units reduced.
	 */
	public double reduceGauge(double gaugeUnits) {
		final double previousValue = this.currentGauge;

		this.currentGauge = Math.max(this.currentGauge - gaugeUnits, 0);
		this.element.reduceLinkedElements(previousValue - this.currentGauge, this, false);

		return previousValue - this.currentGauge;
	}

	public void tick() {
		AbstractBurningElementalReaction.mixin$reduceQuickenGauge(this);
	}

	/**
	 * Reapplies this Elemental Application, given that {@code element} is the same element as this {@code ElementalApplication}.
	 * @param element The element to reapply for this application.
	 * @param gaugeUnits The amount of Elemental Gauge Units to reapply.
	 */
	public void reapply(Element element, double gaugeUnits) {
		reapply(ElementalApplications.gaugeUnits(this.entity, element, gaugeUnits));
	}

	/**
	 * Reapplies this Elemental Application, given that {@code application} has the same element as this one.
	 * @param application The Elemental Application to reapply using this application.
	 */
	public abstract void reapply(ElementalApplication application);

	public abstract ElementalApplication asAura();

	public abstract ElementalApplication asNonAura();

	public void writeData(WriteView view) {
		view.putString("Type", this.type.toString());
		view.putString("Element", this.element.toString());
		view.put("UUID", Uuids.CODEC, uuid);
		view.putBoolean("IsAura", this.isAura);
		view.putDouble("GaugeUnits", this.gaugeUnits);
		view.putDouble("CurrentGauge", this.currentGauge);
		view.putLong("AppliedAt", this.appliedAt);
	}

	public void updateFromData(ReadView view, long syncedAt) {
		final ElementalApplication application = ElementalApplications.fromData(entity, view, syncedAt);

		if (!application.uuid.equals(this.uuid)) throw new ElementalApplicationOperationException(Operation.INVALID_UUID_VALUES, this, application);

		if (application.type != this.type) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_TYPES, this, application);

		if (application.element != this.element) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_ELEMENT, this, application);

		this.currentGauge = application.currentGauge;
		this.gaugeUnits = application.gaugeUnits;
		this.appliedAt = application.appliedAt;
	}

	public static enum Type {
		// Has a specified amount of Gauge Units that decay over time.
		GAUGE_UNIT,
		// Has a specified amount of Gauge Units that are removed after DURATION.
		DURATION;

		public static final Codec<Type> CODEC = Codecs.NON_EMPTY_STRING.xmap(Type::valueOf, Type::toString);
	}

	public static final class Builder {
		public static final Codec<ElementalApplication.Builder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Type.CODEC.optionalFieldOf("type", ElementalApplication.Type.GAUGE_UNIT).forGetter(i -> i.type),
			Element.CODEC.fieldOf("element").forGetter(i -> i.element),
			Codec.BOOL.optionalFieldOf("aura", true).forGetter(i -> i.isAura),
			Codec.DOUBLE.fieldOf("gauge_units").forGetter(i -> i.gaugeUnits),
			Codec.DOUBLE.optionalFieldOf("duration", -1.0).forGetter(i -> i.duration)
		).apply(instance, ElementalApplication.Builder::new));

		private Type type;
		private Element element;
		private boolean isAura;
		private double gaugeUnits;
		private double duration;

		Builder() {
			this.isAura = true;
		}

		public static Text getText(Builder builder) {
			return getText(builder, GAUGE_UNIT_FORMAT, DURATION_FORMAT);
		}

		public static Text getText(Builder builder, @Nullable String gaugeFormat) {
			return getText(builder, ClassInstanceUtil.mapOrNull(gaugeFormat, DecimalFormat::new), DURATION_FORMAT);
		}

		public static Text getText(Builder builder, @Nullable DecimalFormat gaugeFormat) {
			return getText(builder, gaugeFormat, DURATION_FORMAT);
		}

		public static Text getText(Builder builder, @Nullable String gaugeFormat, @Nullable String durationFormat) {
			return getText(
				builder,
				ClassInstanceUtil.<String, DecimalFormat>mapOrNull(gaugeFormat, DecimalFormat::new),
				ClassInstanceUtil.<String, DecimalFormat>mapOrNull(durationFormat, DecimalFormat::new)
			);
		}

		public static Text getText(Builder builder, @Nullable DecimalFormat gaugeFormat, @Nullable DecimalFormat durationFormat) {
			gaugeFormat = JavaScriptUtil.nullishCoalesing(gaugeFormat, GAUGE_UNIT_FORMAT);
			durationFormat = JavaScriptUtil.nullishCoalesing(durationFormat, DURATION_FORMAT);

			return TextHelper.color(
				builder.type == Type.GAUGE_UNIT
					? Text.translatable("formats.seven-elements.elemental_application.gauge_unit", gaugeFormat.format(builder.gaugeUnits), builder.element.getString())
					: Text.translatable("formats.seven-elements.elemental_application.duration", gaugeFormat.format(builder.gaugeUnits), builder.element.getString(), durationFormat.format(builder.duration / 20.0)),
				builder.element.getDamageColor()
			);
		}

		private Builder(Type type, Element element, boolean isAura, double gaugeUnits, double duration) {
			this.type = type;
			this.element = element;
			this.isAura = isAura;
			this.gaugeUnits = gaugeUnits;
			this.duration = duration;
		}

		public Element getElement() {
			return this.element;
		}

		public double getGaugeUnits() {
			return this.gaugeUnits;
		}

		public Builder setType(Type type) {
			this.type = type;

			return this;
		}

		public Builder setElement(Element element) {
			this.element = element;

			return this;
		}

		public Builder setAsAura(boolean isAura) {
			this.isAura = isAura;

			return this;
		}

		public Builder setGaugeUnits(double gaugeUnits) {
			this.gaugeUnits = gaugeUnits;

			return this;
		}

		public Builder setDuration(double duration) {
			this.duration = duration;

			return this;
		}

		public ElementalApplication build(final LivingEntity entity) {
			Objects.requireNonNull(type);
			Objects.requireNonNull(element);

			if (type == Type.DURATION) {
				return ElementalApplications.duration(entity, element, gaugeUnits, duration);
			} else {
				return ElementalApplications.gaugeUnits(entity, element, gaugeUnits, isAura);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;

			if (!(obj instanceof final Builder builder)) return false;

			return this.type == builder.type
				&& this.element == builder.element
				&& this.isAura == builder.isAura
				&& this.gaugeUnits == builder.gaugeUnits
				&& this.duration == builder.duration;
		}
	}
}
