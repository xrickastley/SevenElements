package io.github.xrickastley.sevenelements.interfaces;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.renderer.ElementGlintRenderer;

import net.minecraft.client.render.item.ItemRenderState;

/**
 * An extension of {@link ItemRenderState.Glint} and {@link ItemRenderState.LayerRenderState}, 
 * allowing elemental glints to work.
 *
 * <p>Unfortunately, {@link ItemRenderState.Glint} is an <b>enum</b>, which means that instances
 * reference the same object. In other words, any changes to an enum instance are permanent.
 *
 * <p>To solve this, you <b>must</b> reset the elemental glint state via
 * {@link #sevenelements$resetElementGlintState()} to prevent the previous elemental glint data
 * from leaking between items.
 */
// this is so fuckass but I'm not rewriting the entire item renderer just for this
public interface ElementGlintState {
	/**
	 * Resets the elemental glint state.
	 *
	 * <p>Since {@link ItemRenderState.Glint} is an {@code enum}, this method <b>must</b> be called
	 * every time an item is to be rendered or when the glint is about to be used to prevent the
	 * element glint data from leaking between items.
	 */
	default void sevenelements$resetElementGlintState() {}

	default void sevenelements$setElement(Element element) {}

	default void sevenelements$setGlintType(ElementGlintRenderer.GlintType type) {}

	default boolean sevenelements$hasElementalGlint() {
		return false;
	}

	default boolean sevenelements$hasAttunementGlint() {
		return false;
	}

	default @Nullable Element sevenelements$getElement() {
		return null;
	}

	default void sevenelements$applyElementGlintState(ElementGlintState other) {}
}
