package io.github.xrickastley.sevenelements.util;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public class DelayedRenderer {
	private static final List<BiConsumer<Float, PoseStack>> RENDER_CALLS = new CopyOnWriteArrayList<>();

	public static void add(BiConsumer<Float, PoseStack> consumer) {
		DelayedRenderer.RENDER_CALLS.add(consumer);
	}

	public static void render(final float tickDelta, final PoseStack matrixStack) {
		DelayedRenderer.RENDER_CALLS.forEach(c -> c.accept(tickDelta, matrixStack));
		DelayedRenderer.RENDER_CALLS.clear();
	}
}
