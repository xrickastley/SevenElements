package io.github.xrickastley.sevenelements.util;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.function.TriConsumer;
import org.joml.Matrix4f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3d;

/**
 * Utility class that renders circles around a certain point.
 */
public class CircleRenderer {
	private final List<Renderable> dataArray = new ArrayList<>();
	private double scaleFactor = 1 / MinecraftClient.getInstance().getWindow().getScaleFactor();
	private double x;
	private double y;
	private double z;

	public CircleRenderer(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	/**
	 * Adds a circle with the set point as the center.
	 * @param radius The radius of this circle. This is measured in pixels using the Minecraft window's resolution.
	 * @param percentFilled A percentage in decimal form indicating how much of the circle is filled in. Circles are rendered starting from Quadrant 2 (90°) back to Quadrant 2 (90° + 360°).
	 * @param color The color of this circle.
	 * @return This {@code CircleRenderer}
	 */
	public CircleRenderer add(double radius, double percentFilled, Color color) {
		this.dataArray.add(
			new Circle(radius, percentFilled, color.asARGB())
		);

		return this;
	}

	/**
	 * Adds a circle with the set point as the center.
	 * @param radius The radius of this circle. This is measured in pixels using the Minecraft window's resolution.
	 * @param percentFilled A percentage in decimal form indicating how much of the circle is filled in. Circles are rendered starting from Quadrant 2 (90°) back to Quadrant 2 (90° + 360°).
	 * @param color The color of this circle.
	 * @return This {@code CircleRenderer}
	 */
	public CircleRenderer add(double radius, double percentFilled, int color) {
		this.dataArray.add(
			new Circle(radius, percentFilled, color)
		);

		return this;
	}

	/**
	 * Adds a circle outline with the set point as the center.
	 * @param radius The radius of the inner circle. This is measured in pixels using the Minecraft window's resolution.
	 * @param length The length of the outline. The resulting circle's radius, including the inner circle will be {@code radius + length}.
	 * @param percentFilled A percentage in decimal form indicating how much of the circle is filled in. Circles are rendered starting from Quadrant 2 (90°) back to Quadrant 2 (90° + 360°).
	 * @param color The color of this circle.
	 * @return This {@code CircleRenderer}
	 */
	public CircleRenderer addOutline(double radius, double length, double percentFilled, Color color) {
		this.dataArray.add(
			new CircleOutline(radius, length, percentFilled, color.asARGB())
		);

		return this;
	}

	/**
	 * Adds a circle outline with the set point as the center.
	 * @param radius The radius of the inner circle. This is measured in pixels using the Minecraft window's resolution.
	 * @param length The length of the outline. The resulting circle's radius, including the inner circle will be {@code radius + length}.
	 * @param percentFilled A percentage in decimal form indicating how much of the circle is filled in. Circles are rendered starting from Quadrant 2 (90°) back to Quadrant 2 (90° + 360°).
	 * @param color The color of this circle.
	 * @return This {@code CircleRenderer}
	 */
	public CircleRenderer addOutline(double radius, double length, double percentFilled, int color) {
		this.dataArray.add(
			new CircleOutline(radius, length, percentFilled, color)
		);

		return this;
	}

	/**
	 * Draws all the data added to this {@code CircleRenderer}, then clears all the previous data. This {@code CircleRenderer} can still be used even after {@code draw()} has been invoked, but the data provided before {@code draw()} will be cleared.
	 * @param tessellator The {@code Tessellator} instance. Obtained from {@code Tessellator.getInstance()}.
	 * @param posMatrix The position matrix. Normally obtained from {@code <MatrixStack>.peek().getPositionMatrix()}.
	 */
	public void draw(Matrix4f posMatrix) {
		final Vec3d origin = new Vec3d(x, y, 0)
			.multiply(scaleFactor);

		dataArray.forEach(CircleRenderer.args(Renderable::render, origin, posMatrix));
		dataArray.clear();
	}

	private class Circle implements Renderable {
		protected double radius;
		protected double percentFilled;
		protected int color;

		protected Circle(double radius, double percentFilled, int color) {
			this.radius = radius;
			this.percentFilled = percentFilled;
			this.color = color;
		}

		@Override
		public void render(Vec3d origin, Matrix4f posMatrix) {
			final Tessellator tessellator = Tessellator.getInstance();
			final BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

			bufferBuilder
				.vertex(posMatrix, (float) origin.getX(), (float) origin.getY(), (float) origin.getZ())
				.color(this.color);

			final double subdivisions = Math.ceil(360.0 * this.percentFilled);

			for (int i = 0; i <= subdivisions; i++) {
				final float x = (float) (origin.getX() + (Math.cos((i * (Math.PI / 180)) + (Math.PI / 2)) * (this.radius * scaleFactor)));
				final float y = (float) (origin.getY() - (Math.sin((i * (Math.PI / 180)) + (Math.PI / 2)) * (this.radius * scaleFactor)));

				bufferBuilder
					.vertex(posMatrix, x, y, (float) z)
					.color(this.color);
			}

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableCull();

			RenderSystem.setShader(GameRenderer::getPositionColorProgram);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

			BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		}
	}

	private class CircleOutline extends Circle {
		protected double length;

		protected CircleOutline(double radius, double length, double percentFilled, int color) {
			super(radius, percentFilled, color);

			this.length = length;
		}

		@Override
		public void render(Vec3d origin, Matrix4f posMatrix) {
			final Tessellator tessellator = Tessellator.getInstance();
			final BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

			final float innerRadius = (float) (this.radius * scaleFactor);
			final float totalRadius = (float) ((this.radius + this.length) * scaleFactor);

			final float x = (float) (origin.getX() + (Math.cos((0 * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
			final float y = (float) (origin.getY() - (Math.sin((0 * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));

			bufferBuilder
				.vertex(posMatrix, x, y, (float) origin.getZ())
				.color(this.color);

			final double subdivisions = Math.ceil(360.0 * this.percentFilled);

			for (int i = 0; i <= subdivisions; i++) {
				final float outerX = (float) (origin.getX() + (Math.cos((i * (Math.PI / 180)) + (Math.PI / 2)) * totalRadius));
				final float outerY = (float) (origin.getY() - (Math.sin((i * (Math.PI / 180)) + (Math.PI / 2)) * totalRadius));

				bufferBuilder
					.vertex(posMatrix, outerX, outerY, (float) origin.getZ())
					.color(this.color);

				// Add vertices for the inner circle
				final float innerX = (float) (origin.getX() + (Math.cos((i * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
				final float innerY = (float) (origin.getY() - (Math.sin((i * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));

				bufferBuilder
					.vertex(posMatrix, innerX, innerY, (float) origin.getZ())
					.color(this.color);
			}

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableCull();

			RenderSystem.setShader(GameRenderer::getPositionColorProgram);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

			BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		}
	}

	private interface Renderable {
		public void render(Vec3d origin, Matrix4f posMatrix);
	}

	private static <T, I, J> Consumer<T> args(TriConsumer<T, I, J> consumer, I i, J j) {
		return t -> consumer.accept(t, i, j);
	}
}
