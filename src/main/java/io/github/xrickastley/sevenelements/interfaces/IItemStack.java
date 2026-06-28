package io.github.xrickastley.sevenelements.interfaces;

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
}
