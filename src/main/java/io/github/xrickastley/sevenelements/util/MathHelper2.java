package io.github.xrickastley.sevenelements.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class MathHelper2 {
	public static double endOffset(double value, double offset, double start, double end) {
		return Math.max(Math.max(value, start) - (end - offset), 0) / offset;
	}

	/**
	 * Rounds a value to the specified amount of significant figures.
	 * @param value The value to round.
	 * @param sigFigs The amount of significant figures.
	 */
	public static float roundTo(float value, int sigFigs) {
		if (sigFigs <= 0) throw new ArithmeticException("The amount of significant figures provided must be greater than 0!");

		if (value < 1) return (float) (Math.round(value * Math.pow(10, sigFigs)) / Math.pow(10, sigFigs));

		final int power = String.valueOf((int) value).length();
		final double decimal = value / Math.pow(10, power);

		return (float) (Math.round(decimal * Math.pow(10, sigFigs)) / Math.pow(10, sigFigs) * Math.pow(10, power));
	}

	/**
	 * Creates a {@code BlockPos} from the provided {@code Vec3d}. <br> <br>
	 *
	 * This implementation ensures that the provided {@code Vec3d} relative to the world will
	 * point to the same {@code BlockPos} returned by this method.
	 *
	 * @param pos The position to turn into a {@code BlockPos}.
	 */
	public static BlockPos asBlockPos(Vec3d pos) {
		return new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
	}

	public static boolean inRange(double value, double start, double end) {
		return start <= value && value <= end;
	}
}
