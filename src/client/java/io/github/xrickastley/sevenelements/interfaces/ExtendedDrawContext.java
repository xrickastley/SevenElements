package io.github.xrickastley.sevenelements.interfaces;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderPipelines;

public interface ExtendedDrawContext {
	default void sevenelements$drawCircle(float x, float y, float radius) {
		this.sevenelements$drawCircle(SevenElementsRenderPipelines.CIRCLE, x, y, radius);
	}

	default void sevenelements$drawCircle(RenderPipeline pipeline, float x, float y, float radius) {
		this.sevenelements$drawCircle(pipeline, x, y, radius, -1);
	}

	default void sevenelements$drawCircle(RenderPipeline pipeline, float x, float y, float radius, int color) {

	}
}
