package io.github.xrickastley.sevenelements.util;

import java.util.function.Function;

import net.minecraft.util.math.MathHelper;

public enum Ease {
	LINEAR				(x -> x),
	IN_SINE				(x -> 1 - Math.cos((x * Math.PI) / 2)),
	OUT_SINE			(x -> Math.sin((x * Math.PI) / 2)),
	IN_OUT_SINE			(x -> -(Math.cos(Math.PI * x) - 1) / 2),
	IN_QUAD				(x -> x * x),
	OUT_QUAD			(x -> 1 - Math.pow(1 - x, 2)),
	IN_OUT_QUAD			(x -> x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2),
	IN_CUBIC			(x -> x * x * x),
	OUT_CUBIC			(x -> 1 - Math.pow(1 - x, 3)),
	IN_OUT_CUBIC		(x -> x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2),
	IN_QUART			(x -> x * x * x * x),
	OUT_QUART			(x -> 1 - Math.pow(1 - x, 4)),
	IN_OUT_QUART		(x -> x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2);

	private final Function<Double, Double> easeFunction;

	Ease(Function<Double, Double> easeFunction) {
		this.easeFunction = easeFunction;
	}

	public double apply(double range) {
		return easeFunction.apply(range);
	}

	public double applyLerp(double delta, double start, double end) {
		return MathHelper.lerp(easeFunction.apply(MathHelper.clamp(delta, 0, 1)), start, end);
	}

	public double applyLerpProgress(double value, double start, double end) {
		return easeFunction.apply(
			MathHelper.clamp(MathHelper.getLerpProgress(value, start, end), 0, 1)
		);
	}
}
