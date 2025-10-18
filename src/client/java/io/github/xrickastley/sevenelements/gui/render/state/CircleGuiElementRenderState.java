package io.github.xrickastley.sevenelements.gui.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;

public record CircleGuiElementRenderState(RenderPipeline pipeline, Matrix3x2f matrix, float x, float y, float radius, int color, @Nullable ScreenRect scissorArea, @Nullable ScreenRect bounds) implements SimpleGuiElementRenderState {
	public CircleGuiElementRenderState(RenderPipeline pipeline, Matrix3x2f matrix, float x, float y, float radius, int color, @Nullable ScreenRect scissorArea) {
		this(pipeline, matrix, x, y, radius, color, scissorArea, createBounds(x, y, radius, matrix, scissorArea));
	}

	@Nullable
	private static ScreenRect createBounds(float x, float y, float radius, Matrix3x2f matrix, @Nullable ScreenRect scissorArea) {
		final ScreenRect screenRect = new ScreenRect(
			(int) Math.floor(x - radius),
			(int) Math.floor(y - radius),
			(int) Math.ceil(x + radius),
			(int) Math.ceil(y + radius)
		).transformEachVertex(matrix);

		return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
	}

	@Override
	public void setupVertices(VertexConsumer vertices) {
		vertices.vertex(this.matrix, this.x - this.radius, this.y + this.radius).texture(0, 0).color(this.color);
		vertices.vertex(this.matrix, this.x + this.radius, this.y + this.radius).texture(1, 0).color(this.color);
		vertices.vertex(this.matrix, this.x + this.radius, this.y - this.radius).texture(1, 1).color(this.color);
		vertices.vertex(this.matrix, this.x - this.radius, this.y - this.radius).texture(0, 1).color(this.color);
	}

	@Override
	public TextureSetup textureSetup() {
		return TextureSetup.empty();
	}
}
