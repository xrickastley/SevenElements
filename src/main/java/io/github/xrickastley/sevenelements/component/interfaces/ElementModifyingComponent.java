package io.github.xrickastley.sevenelements.component.interfaces;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;

import net.minecraft.text.Text;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;

/**
 * An interface for data components that modify the elements, specifically the
 * {@link ElementalInfusionComponent} <br> <br>
 *
 * This component interface actually doesn't do much, only handling appending the special icons
 * to modified infusions when the item's name is needed.
 */
public interface ElementModifyingComponent {
	public static void addElementModifyingComponent(ComponentKey<? extends ElementModifyingComponent> key) {
		ElementModifyingComponentImpl.addComponent(key);
	}

	/**
	 * {@return the symbol appended to the elemental infusion text}
	 */
	public Text getSymbol();

	/**
	 * {@return whether this {@code ElementModifyingComponent} should modify the provided
	 * {@link ElementalInfusionComponent}}
	 *
	 * @param infusion The elemental infusion component to be modified.
	 */
	default boolean shouldModify(ElementalInfusionComponent infusion) {
		return true;
	}
}
