package io.github.xrickastley.sevenelements.element;

import java.util.UUID;

import io.github.xrickastley.sevenelements.element.ElementalApplication.Type;
import io.github.xrickastley.sevenelements.exception.ElementalApplicationOperationException.Operation;
import io.github.xrickastley.sevenelements.exception.ElementalApplicationOperationException;
import io.github.xrickastley.sevenelements.util.NbtHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

public class ElementalApplications {
	/**
	 * Creates an Elemental Application using Elemental Gauge Units.
	 * @param entity The entity to create an Elemental Application for.
	 * @param element The Element of this Elemental Application.
	 * @param gaugeUnits The amount of Elemental Gauge Units this Elemental Application has.
	 */
	public static ElementalApplication gaugeUnits(LivingEntity entity, Element element, double gaugeUnits) {
		return ElementalApplications.gaugeUnits(entity, element, gaugeUnits, true);
	}

	/**
	 * Creates an Elemental Application using Elemental Gauge Units.
	 * @param entity The entity to create an Elemental Application for.
	 * @param element The Element of this Elemental Application.
	 * @param gaugeUnits The amount of Elemental Gauge Units this Elemental Application has.
	 * @param aura Whether this Elemental Application is an Aura Element. This means that the <a href="https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory#Aura_Tax">Aura Tax</a> applies to the current gauge units of this Element.
	 */
	public static ElementalApplication gaugeUnits(LivingEntity entity, Element element, double gaugeUnits, boolean aura) {
		return new GaugeUnitElementalApplication(entity, element, UUID.randomUUID(), gaugeUnits, aura);
	}

	/**
	 * Creates an Elemental Application with a specified duration.
	 * @param entity The entity to create an Elemental Application for.
	 * @param element The Element of this Elemental Application.
	 * @param gaugeUnits The amount of Gauge Units this Elemental Application has.
	 * @param duration The duration of the Elemental Application, in ticks.
	 */
	public static ElementalApplication duration(LivingEntity entity, Element element, double gaugeUnits, double duration) {
		return new DurationElementalApplication(entity, element, UUID.randomUUID(), gaugeUnits, duration);
	}

	/**
	 * Creates an Elemental Application from an NBT.
	 * @param entity The entity to create an Elemental Application for.
	 * @param nbt The NBT to create the Elemental Application from.
	 */
	public static ElementalApplication fromNbt(LivingEntity entity, NbtElement nbt) {
		return ElementalApplications.fromNbt(entity, nbt, entity.getWorld().getTime());
	}

	/**
	 * Creates an Elemental Application from an NBT.
	 * @param entity The entity to create an Elemental Application for.
	 * @param nbt The NBT to create the Elemental Application from.
	 * @param syncedAt The world time this Elemental Application was last synced at.
	 */
	public static ElementalApplication fromNbt(LivingEntity entity, NbtElement nbt, long syncedAt) {
		if (!(nbt instanceof final NbtCompound compound)) throw new ElementalApplicationOperationException(Operation.INVALID_NBT_DATA, null, null);

		final Type type = NbtHelper.get(compound, "Type", ElementalApplication.Type.CODEC);

		return type == Type.GAUGE_UNIT
			? GaugeUnitElementalApplication.fromNbt(entity, compound, syncedAt)
			: DurationElementalApplication.fromNbt(entity, compound, syncedAt);
	}

	/**
	 * Creates a new Elemental Application Builder. <br> <br>
	 *
	 * A builder is used for creating an Elemental Application without a {@code LivingEntity} to
	 * "own" the Elemental Application as of the moment. <br> <br>
	 *
	 * Once an entity exists, an instance of {@code ElementalApplication} may be created through
	 * {@link ElementalApplication.Builder#build(LivingEntity) Builder#build()}.
	 */
	public static ElementalApplication.Builder builder() {
		return new ElementalApplication.Builder();
	}

	/**
	 * Gets the timer text of a provided {@link ElementalApplication}, or the standard text if
	 * unapplicable.
	 */
	public static Text getTimerText(ElementalApplication application) {
		return application instanceof final DurationElementalApplication durApp
			? durApp.getTimerText()
			: application.getText();
	}
}
