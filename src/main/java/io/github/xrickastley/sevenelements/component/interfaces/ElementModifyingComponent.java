package io.github.xrickastley.sevenelements.component.interfaces;

import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import net.minecraft.text.Text;

/**
 * An interface for data components that modify the elements, specifically the
 * {@link ElementalInfusionComponent} <br> <br>
 *
 * This component interface actually doesn't do much, only handling appending the special icons
 * to modified infusions when the item's name is needed. 
 */
public interface ElementModifyingComponent {
	public Text getSymbol();
}
