package io.github.xrickastley.sevenelements.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

public interface IEntity {
	/**
	 * {@return whether the entity is fully submerged in a fluid in {@code fluidTag}} <br> <br>
	 * 
	 * This is a stricter version of {@link Entity#isSubmergedIn(TagKey) isSubmergedIn}, requiring
	 * the entity to be fully submerged in a fluid in {@code fluidTag} instead of just checking
	 * whether the fluid in the entity's eye level is a fluid in {@code fluidTag}. <br> <br>
	 * 
	 * This method takes into account the entire height of the entity, as given by 
	 * {@link Entity#getHeight getHeight}.
	 *
	 * @see Entity#isSubmergedIn(TagKey)
	 */
	default boolean sevenelements$isFullySubmergedIn(TagKey<Fluid> fluidTag) {
		return false;
	}
}
