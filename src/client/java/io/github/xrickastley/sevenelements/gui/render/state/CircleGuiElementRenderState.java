package io.github.xrickastley.sevenelements.gui.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

public record CircleGuiElementRenderState(RenderPipeline pipeline, Matrix3x2f matrix, float x, float y, float radius, int color, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState {
	public CircleGuiElementRenderState(RenderPipeline pipeline, Matrix3x2f matrix, float x, float y, float radius, int color, @Nullable ScreenRectangle scissorArea) {
		this(pipeline, matrix, x, y, radius, color, scissorArea, createBounds(x, y, radius, matrix, scissorArea));
	}

	@Nullable
	private static ScreenRectangle createBounds(float x, float y, float radius, Matrix3x2f matrix, @Nullable ScreenRectangle scissorArea) {
		final ScreenRectangle screenRect = new ScreenRectangle(
			(int) Math.floor(x - radius),
			(int) Math.floor(y - radius),
			(int) Math.ceil(x + radius),
			(int) Math.ceil(y + radius)
		).transformMaxBounds(matrix);

		return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
	}

	@Override
	public void buildVertices(VertexConsumer vertices) {
		vertices.addVertexWith2DPose(this.matrix, this.x - this.radius, this.y + this.radius).setUv(0, 0).setColor(this.color);
		vertices.addVertexWith2DPose(this.matrix, this.x + this.radius, this.y + this.radius).setUv(1, 0).setColor(this.color);
		vertices.addVertexWith2DPose(this.matrix, this.x + this.radius, this.y - this.radius).setUv(1, 1).setColor(this.color);
		vertices.addVertexWith2DPose(this.matrix, this.x - this.radius, this.y - this.radius).setUv(0, 1).setColor(this.color);
	}

	@Override
	public TextureSetup textureSetup() {
		return TextureSetup.noTexture();
	}
}
