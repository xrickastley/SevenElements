package io.github.xrickastley.sevenelements.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import net.minecraft.client.util.math.MatrixStack;

public class DelayedRenderer {
	private static final List<BiConsumer<Float, MatrixStack>> RENDER_CALLS = new CopyOnWriteArrayList<>();

	public static void add(BiConsumer<Float, MatrixStack> consumer) {
		DelayedRenderer.RENDER_CALLS.add(consumer);
	}

	public static void render(final float tickDelta, final MatrixStack matrixStack) {
		DelayedRenderer.RENDER_CALLS.forEach(c -> c.accept(tickDelta, matrixStack));
		DelayedRenderer.RENDER_CALLS.clear();
	}
}
