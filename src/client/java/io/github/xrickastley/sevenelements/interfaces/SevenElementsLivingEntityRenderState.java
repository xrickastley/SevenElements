package io.github.xrickastley.sevenelements.interfaces;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.renderer.feature.ElementFeatureRenderer;
import io.github.xrickastley.sevenelements.renderer.feature.ElementGaugeFeatureRenderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * An injected interface that allows access to Seven Elements-specific rendering states from the
 * {@link LivingEntityRenderState}, as well as other useful properties injected by Seven Elements,
 * as they are currently implemented via mixin and are not accessible normally.
 */
public interface SevenElementsLivingEntityRenderState {
	/**
	 * Fills in this {@code SevenElementsLivingEntityRenderState} with the provided
	 * {@code entity}.
	 *
	 * <p>Adhering to Mojang's decoupling of the entity instance from the render state, the
	 * provided entity instance must <b>not</b> be stored inside the render state, as it is only
	 * provided to this method for data extraction.
	 *
	 * @param entity The entity to fill the data of this render state with.
	 */
	default void sevenelements$fillRenderState(LivingEntity entity, float tickDelta) {}

	default boolean sevenelements$isDead() {
		return false;
	}

	default boolean sevenelements$isClientPlayer() {
		return false;
	}

	default boolean sevenelements$isFrozen() {
		return false;
	}

	default Vec3 sevenelements$getBoundingBoxLength() {
		return Vec3.ZERO;
	}

	default List<ElementFeatureRenderer.ElementState> sevenelements$getElementStates() {
		return Collections.emptyList();
	}

	default List<ElementGaugeFeatureRenderer.ElementGaugeState> sevenelements$getGaugeStates() {
		return Collections.emptyList();
	}

	default @Nullable Element sevenelements$getCrystallizeShieldElement() {
		return null;
	}
}
