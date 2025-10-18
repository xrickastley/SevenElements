package io.github.xrickastley.sevenelements.element;

import com.mojang.serialization.Codec;

import java.text.DecimalFormat;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.events.ElementEvents;
import io.github.xrickastley.sevenelements.exception.ElementalApplicationOperationException.Operation;
import io.github.xrickastley.sevenelements.exception.ElementalApplicationOperationException;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;
import io.github.xrickastley.sevenelements.util.TextHelper;
import io.github.xrickastley.sevenelements.util.ViewHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.world.World;

public final class DurationElementalApplication extends ElementalApplication {
	private double duration;

	DurationElementalApplication(LivingEntity entity, Element element, UUID uuid, double gaugeUnits, double duration) {
		super(Type.DURATION, entity, element, uuid, gaugeUnits, true);

		this.duration = duration;
	}

	static ElementalApplication fromData(LivingEntity entity, ReadView view, long syncedAt) {
		final Element element = ViewHelper.get(view, "Element", Element.CODEC);
		final UUID uuid = ViewHelper.get(view, "UUID", Uuids.CODEC);
		final double gaugeUnits = ViewHelper.get(view, "GaugeUnits", Codec.doubleRange(0, Double.MAX_VALUE));
		final double duration = ViewHelper.get(view, "Duration", Codec.doubleRange(0, Double.MAX_VALUE));

		final var application = new DurationElementalApplication(entity, element, uuid, gaugeUnits, duration);

		application.currentGauge = ViewHelper.get(view, "CurrentGauge", Codec.doubleRange(0, Double.MAX_VALUE));
		application.appliedAt = ViewHelper.get(view, "AppliedAt", Codec.LONG);

		return application;
	}

	public double getDuration() {
		return this.duration;
	}

	@Override
	protected double getDefaultDecayRate() {
		return 0;
	}

	@Override
	public int getRemainingTicks() {
		return (int) (appliedAt + duration - entity.getWorld().getTime());
	}

	@Override
	public Text getText(@Nullable DecimalFormat gaugeFormat, @Nullable DecimalFormat durationFormat) {
		gaugeFormat = JavaScriptUtil.nullishCoalesing(gaugeFormat, GAUGE_UNIT_FORMAT);
		durationFormat = JavaScriptUtil.nullishCoalesing(durationFormat, DURATION_FORMAT);

		return TextHelper.color(
			Text.translatable("formats.seven-elements.elemental_application.duration", gaugeFormat.format(this.currentGauge), this.element.getString(), durationFormat.format(this.duration / 20.0)),
			this.element.getDamageColor()
		);
	}

	public Text getTimerText() {
		return this.getTimerText(GAUGE_UNIT_FORMAT, DURATION_FORMAT);
	}

	public Text getTimerText(@Nullable String gaugeFormat) {
		return this.getTimerText(
			ClassInstanceUtil.mapOrNull(gaugeFormat, DecimalFormat::new),
			DURATION_FORMAT
		);
	}

	public Text getTimerText(@Nullable DecimalFormat gaugeFormat) {
		return this.getTimerText(gaugeFormat, DURATION_FORMAT);
	}

	public Text getTimerText(@Nullable String gaugeFormat, @Nullable String durationFormat) {
		return this.getTimerText(
			ClassInstanceUtil.<String, DecimalFormat>mapOrNull(gaugeFormat, DecimalFormat::new),
			ClassInstanceUtil.<String, DecimalFormat>mapOrNull(durationFormat, DecimalFormat::new)
		);
	}

	public Text getTimerText(@Nullable DecimalFormat gaugeFormat, @Nullable DecimalFormat durationFormat) {
		gaugeFormat = JavaScriptUtil.nullishCoalesing(gaugeFormat, GAUGE_UNIT_FORMAT);
		durationFormat = JavaScriptUtil.nullishCoalesing(durationFormat, DURATION_FORMAT);

		return TextHelper.color(
			Text.translatable("formats.seven-elements.elemental_application.duration.timer", gaugeFormat.format(this.currentGauge), this.element.getString(), durationFormat.format(this.getRemainingTicks() / 20.0)),
			this.element.getDamageColor()
		);
	}

	/**
	 * {@inheritDoc} <br> <br>
	 *
	 * This implementation guarantees this to be {@code true} when {@code currentGauge} reaches
	 * {@code 0} or when the current world time, given by {@link LivingEntity#getWorld()}
	 * {@link World#getTime() .getTime()} exceeds {@code duration + appliedAt}.
	 */
	@Override
	public boolean isEmpty() {
		return this.currentGauge <= 0 || entity.getWorld().getTime() >= (this.appliedAt + this.duration);
	}

	@Override
	public void reapply(ElementalApplication application) {
		if (application.element != this.element) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_ELEMENT, this, application);

		if (application.type != this.type || !(application instanceof final DurationElementalApplication durationApp)) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_TYPES, this, application);

		this.gaugeUnits = Math.max(this.gaugeUnits, application.gaugeUnits);
		this.currentGauge = this.gaugeUnits;
		this.duration = durationApp.duration;
		this.appliedAt = durationApp.appliedAt;

		ElementEvents.REAPPLIED.invoker().onElementReapplied(this.element, this);

		ElementComponent.sync(this.entity);
	}

	@Override
	public ElementalApplication asAura() {
		throw new UnsupportedOperationException("This method is unsupported on Elemental Applications with a DURATION type!");
	}

	@Override
	public ElementalApplication asNonAura() {
		throw new UnsupportedOperationException("This method is unsupported on Elemental Applications with a DURATION type!");
	}

	@Override
	public void writeData(WriteView view) {
		super.writeData(view);

		view.putDouble("Duration", this.duration);
	}

	@Override
	public void updateFromData(ReadView view, long syncedAt) {
		super.updateFromData(view, syncedAt);

		final ElementalApplication application = ElementalApplications.fromData(entity, view, syncedAt);

		this.duration = ((DurationElementalApplication) application).duration;
		this.appliedAt = application.appliedAt;
	}

	@Override
	public String toString() {
		return String.format(
			"%s@%s[type=DURATION, element=%s, gaugeUnits=%s, duration=%.2f]",
			this.getClass().getSimpleName(),
			Integer.toHexString(this.hashCode()),
			this.getElement().toString(),
			this.getGaugeUnits(),
			this.getDuration()
		);
	}
}
