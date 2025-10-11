package io.github.xrickastley.sevenelements.interfaces;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;

/**
 * A class that allows access to the entity tied to the provided render state. <br> <br>
 * 
 * This interface directly goes against the aim of decoupling the entity from rendering by directly
 * adding it to the state, so <b>only</b> use it when the entity is necessary, such as rendering 
 * "complex" components like the {@link io.github.xrickastley.sevenelements.component.ElementComponent ElementComponent})
 */
public interface EntityAwareRenderState {
	default @Nullable Entity sevenelements$getEntity() {
		return null;
	}

	default void sevenelements$setEntity(Entity entity) {}
}
