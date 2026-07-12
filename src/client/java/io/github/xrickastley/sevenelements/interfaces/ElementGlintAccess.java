package io.github.xrickastley.sevenelements.interfaces;

import org.spongepowered.asm.mixin.Unique;

public interface ElementGlintAccess {
	@Unique
	default void sevenelements$setElementGlintState(ElementGlintState state) {}
}
