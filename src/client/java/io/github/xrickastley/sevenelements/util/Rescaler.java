package io.github.xrickastley.sevenelements.util;

import net.minecraft.client.Minecraft;

/**
 * Utility class that rescales components according to the current scaled height and width given the original height and width.
 */
public class Rescaler {
	protected double originalX;
	protected double originalY;

	public Rescaler(double originalX, double originalY) {
		this.originalX = originalX;
		this.originalY = originalY;
	}

	/**
	 * Rescales {@code x} based on the {@code originalX}, returning the scaled x-coordinate based on Minecraft's rendering system.
	 * @param x The x-coordinate to rescale.
	 * @return The rescaled x-coordinate. Width within Minecraft's rendering system and the Window itself is different, with the factor given by {@code MinecraftClient.getInstance().getWindow().getScaleFactor()}. This method returns the rescaled x-coordinate based on Minecraft's own scaled width.
	 */
	public int rescaleX(double x) {
		return (int) ((x / originalX) * Minecraft.getInstance().getWindow().getGuiScaledWidth());
	}

	/**
	 * Rescales {@code x} based on the {@code originalX}, returning the scaled x-coordinate based on the Minecraft window.
	 * @param x The x-coordinate to rescale.
	 * @return The rescaled x-coordinate. Width within Minecraft's rendering system and the Window itself is different, with the factor given by {@code MinecraftClient.getInstance().getWindow().getScaleFactor()}. This method returns the rescaled x-coordinate based on the Minecraft window.
	 */
 	public int rescaleXWindow(double x) {
		return (int) (this.rescaleX(x) * Minecraft.getInstance().getWindow().getGuiScale());
	}

	/**
	 * Rescales {@code y} based on the {@code originalY}, returning the scaled y-coordinate based on Minecraft's rendering system.
	 * @param y The y-coordinate to rescale.
	 * @return The rescaled y-coordinate. Height within Minecraft's rendering system and the Window itself is different, with the factor given by {@code MinecraftClient.getInstance().getWindow().getScaleFactor()}. This method returns the rescaled y-coordinate based on Minecraft's own scaled Height.
	 */
	public int rescaleY(double y) {
		return (int) ((y / originalY) * Minecraft.getInstance().getWindow().getGuiScaledHeight());
	}

	/**
	 * Rescales {@code y} based on the {@code originalY}, returning the scaled y-coordinate based on the Minecraft window.
	 * @param y The y-coordinate to rescale.
	 * @return The rescaled y-coordinate. Height within Minecraft's rendering system and the Window itself is different, with the factor given by {@code MinecraftClient.getInstance().getWindow().getScaleFactor()}. This method returns the rescaled y-coordinate based on the Minecraft window.
	 */
 	public int rescaleYWindow(double y) {
		return (int) (this.rescaleY(y) * Minecraft.getInstance().getWindow().getGuiScale());
	}

	/**
	 * Gets the rescale factor for resizing whole objects. This returns the minimum of either {@code originalX / windowX} or {@code originalY / windowY}
	 * @return The rescale factor for resizing whole objects.
	 */
	public double getRescaleFactorWindow() {
		final int windowX = (int) (Minecraft.getInstance().getWindow().getGuiScaledWidth() * Minecraft.getInstance().getWindow().getGuiScale());
		final int windowY = (int) (Minecraft.getInstance().getWindow().getGuiScaledHeight() * Minecraft.getInstance().getWindow().getGuiScale());

		return Math.min(windowX / originalX, windowY / originalY);
	}

	/**
	 * Gets the rescale factor for resizing whole objects. This returns the minimum of either {@code originalX / windowX} or {@code originalY / windowY}, divided by the window's scale factor.
	 * @return The rescale factor for resizing whole objects.
	 */
	public double getRescaleFactor() {
		return getRescaleFactorWindow() / Minecraft.getInstance().getWindow().getGuiScale();
	}
}
