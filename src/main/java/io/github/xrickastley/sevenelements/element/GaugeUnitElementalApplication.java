package io.github.xrickastley.sevenelements.element;

import com.mojang.serialization.Codec;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.events.ElementEvents;
import io.github.xrickastley.sevenelements.exception.ElementalApplicationOperationException.Operation;
import io.github.xrickastley.sevenelements.exception.ElementalApplicationOperationException;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;
import io.github.xrickastley.sevenelements.util.NbtHelper;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.MathHelper;

public final class GaugeUnitElementalApplication extends ElementalApplication {
	private double decayRate;

	GaugeUnitElementalApplication(LivingEntity entity, Element element, UUID uuid, double gaugeUnits, boolean isAura) {
		super(Type.GAUGE_UNIT, entity, element, uuid, gaugeUnits, isAura);

		this.decayRate = GaugeUnitElementalApplication.getDefaultDecayRate(gaugeUnits);

		// Aura tax.
		if (this.isAura && element.hasAuraTax()) this.currentGauge *= 0.8;
	}

	static ElementalApplication fromNbt(LivingEntity entity, NbtCompound nbt, long syncedAt) {
		final Element element = NbtHelper.get(nbt, "Element", Element.CODEC);
		final UUID uuid = NbtHelper.get(nbt, "UUID", Uuids.CODEC);
		final double gaugeUnits = NbtHelper.get(nbt, "GaugeUnits", Codec.doubleRange(0, Double.MAX_VALUE));
		final double currentGauge = NbtHelper.get(nbt, "CurrentGauge", Codec.doubleRange(0, Double.MAX_VALUE));
		final boolean isAura = NbtHelper.get(nbt, "IsAura", Codec.BOOL);

		final var application = new GaugeUnitElementalApplication(entity, element, uuid, gaugeUnits, isAura);

		final double syncedGaugeDeduction = Math.max(entity.getWorld().getTime() - syncedAt, 0) * application.getDecayRate();
		application.currentGauge = MathHelper.clamp(currentGauge - syncedGaugeDeduction, 0, application.gaugeUnits);
		application.appliedAt = NbtHelper.get(nbt, "AppliedAt", Codec.LONG);

		return application;
	}

	/**
	 * Gets the default decay rate per tick, derived from <a href="https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory#Aura_Duration_and_Decay_Rate">
	 * Elemental Gauge Theory: Aura Duration and Decay Rate</a>.
	 */
	private static double getDefaultDecayRate(final double gaugeUnits) {
		// Currently in s/GU
		double decayRate = (35 / (4 * gaugeUnits)) + (25.0 / 8.0);
		// Now in ticks/GU
		double decayRateTicks = decayRate / 0.05;
		// Now a GU/tick, allowing us to tick down the gauge easily.
		return 1 / decayRateTicks;
	}

	/**
	 * Gets the current decay rate in {@code Gauge Units/tick}.
	 */
	private double getDecayRate() {
		final @Nullable Function<ElementalApplication, Number> customDecayRate = this.element.getCustomDecayRate();

		return customDecayRate == null
			? this.getDefaultDecayRate()
			: customDecayRate.apply(this).doubleValue();
	}

	@Override
	protected double getDefaultDecayRate() {
		return this.decayRate;
	}

	@Override
	public int getRemainingTicks() {
		// GU/tick -> ticks/GU
		final double decayRate = 1 / this.getDecayRate();

		return (int) (decayRate * this.currentGauge);
	}

	@Override
	public Text getText(@Nullable DecimalFormat gaugeFormat, @Nullable DecimalFormat durationFormat) {
		gaugeFormat = JavaScriptUtil.nullishCoalesing(gaugeFormat, GAUGE_UNIT_FORMAT);

		return TextHelper.color(
			Text.translatable("formats.seven-elements.elemental_application.gauge_unit", gaugeFormat.format(this.currentGauge), this.element.getString()),
			this.element.getDamageColor()
		);
	}

	/**
	 * {@inheritDoc} <br> <br>
	 *
	 * This implementation guarantees this to be {@code true} when {@code currentGauge} reaches
	 * {@code 0}.
	 */
	@Override
	public boolean isEmpty() {
		return this.currentGauge <= 0;
	}

	@Override
	public void tick() {
		super.tick();

		this.currentGauge -= this.getDecayRate();
	}

	@Override
	public void reapply(ElementalApplication application) {
		if (application.element != this.element) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_ELEMENT, this, application);

		if (application.type != this.type || !(application instanceof final GaugeUnitElementalApplication guApp)) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_TYPES, this, application);

		// The current gauge, handled by currentGauge, is always the most of both applications.
		this.gaugeUnits = Math.max(this.gaugeUnits, guApp.gaugeUnits);
		this.currentGauge = gaugeUnits;

		// The decay rate, handled by gaugeUnits, is always the lesser of both applications, given that the element has Decay Inheritance.
		this.decayRate = this.element.hasDecayInheritance()
			? Math.min(this.decayRate, guApp.decayRate)
			: guApp.decayRate;

		if (this.isAura) this.currentGauge *= 0.8;

		ElementEvents.REAPPLIED.invoker().onElementReapplied(this.element, this);

		ElementComponent.sync(this.entity);
	}

	@Override
	public ElementalApplication asAura() {
		return new GaugeUnitElementalApplication(entity, element, UUID.randomUUID(), gaugeUnits, true);
	}

	@Override
	public ElementalApplication asNonAura() {
		return new GaugeUnitElementalApplication(entity, element, UUID.randomUUID(), gaugeUnits, false);
	}

	@Override
	public String toString() {
		return String.format(
			"%s@%s[type=GAUGE_UNIT, element=%s, gaugeUnits=%2f, currentGauge=%.2f]",
			this.getClass().getSimpleName(),
			Integer.toHexString(this.hashCode()),
			this.getElement().toString(),
			this.getGaugeUnits(),
			this.getCurrentGauge()
		);
	}
}
