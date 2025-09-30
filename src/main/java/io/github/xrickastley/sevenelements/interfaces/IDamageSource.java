package io.github.xrickastley.sevenelements.interfaces;

public interface IDamageSource {
	/**
	 * Sets whether the DMG text should be displayed or not.
	 * 
	 * @param display Whether the DMG text should be displayed or not.
	 */
	default void sevenelements$shouldDisplayDamage(boolean display) {}

	/**
	 * Whether the DMG text should be displayed or not.
	 */
	default boolean sevenelements$displayDamage() {
		return true;
	}
}
