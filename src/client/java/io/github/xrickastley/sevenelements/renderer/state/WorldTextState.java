package io.github.xrickastley.sevenelements.renderer.state;

import net.minecraft.util.FormattedCharSequence;

public record WorldTextState(
	FormattedCharSequence text,
	double x,
	double y,
	double z,
	int color,
	float size,
	boolean center,
	float offset,
	boolean visibleThroughObjects
) {}
