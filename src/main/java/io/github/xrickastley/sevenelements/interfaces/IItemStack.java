package io.github.xrickastley.sevenelements.interfaces;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public interface IItemStack {
	/**
	 * {@return the custom name of the stack if it exists, or the item's name}
	 * This is the unmodified version of {@link ItemStack#getName()}, and should <b>only</b> be
	 * used in contexts where the stack's name is being edited.
	 *
	 * @implNote This method only accounts for name changes made by Seven Elements. Other mods that
	 * modify the item name may still affect the <i>true</i> name.
	 *
	 * @see ItemStack#getName()
	 */
	default Text sevenelements$getTrueName() {
		return null;
	}

	/**
	 * Checks if an elemental glint effect should be applied when the item stack is rendered.
	 * This is affected by the value of the {@link DataComponentTypes#ENCHANTMENT_GLINT_OVERRIDE}
	 * component.
	 *
	 * <p>By default, returns true if the stack has an elemental infusion or if
	 * {@link #sevenelements$hasAttunementGlint()} is {@code true}.
	 */
	default boolean sevenelements$hasElementalGlint() {
		return false;
	}

	/**
	 * Checks if the elemental attunement glint effect should be applied when the item stack is
	 * rendered.
	 * This is affected by the value of the {@link DataComponentTypes#ENCHANTMENT_GLINT_OVERRIDE}
	 * component.
	 *
	 * <p>By default, returns true if the stack has an elemental attunement that matches its
	 * elemental infusion or if the stack contains the {@link DataComponentTypes#EQUIPPABLE}
	 * component.
	 */
	default boolean sevenelements$hasAttunementGlint() {
		return false;
	}
}
