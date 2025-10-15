package io.github.xrickastley.sevenelements.util;

import java.util.function.Function;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import net.minecraft.client.render.BufferBuilder;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayer;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderPipelines;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderer;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class SphereRenderer {
	private static final BufferAllocator allocator = new BufferAllocator(SevenElementsRenderLayer.getSphere().getExpectedBufferSize());
	private SphereRenderer() {}

	/**
	 * Render a sphere centered at (x,y,z) in world coordinates.
	 *
	 * @param matrices The current {@code MatrixStack}.
	 * @param origin The origin point.
	 * @param radius Sphere radius (in blocks)
	 * @param latSteps Vertical subdivisions
	 * @param lonSteps Horizontal subdivisions
	 * @param color An ARGB int {@code 0xAARRGGBB}
	 */
	public static void render(MatrixStack matrices, Vec3d origin, float radius, int latSteps, int lonSteps, int color) {
		SphereRenderer.render(matrices, origin, radius, latSteps, lonSteps, pos -> color);
	}

	/**
	 * Render a sphere centered at (x,y,z) in world coordinates.
	 *
	 * @param matrices The current {@code MatrixStack}.
	 * @param origin The origin point.
	 * @param radius Sphere radius (in blocks)
	 * @param latSteps Vertical subdivisions
	 * @param lonSteps Horizontal subdivisions
	 * @param colorFunc A function taking in a {@code Vec3d} and returns an ARGB int {@code 0xAARRGGBB}
	 */
	public static void render(MatrixStack matrices, Vec3d origin, float radius, int latSteps, int lonSteps, Function<Vec3d, Integer> colorFunc) {
		if (latSteps < 2) latSteps = 2;
		if (lonSteps < 3) lonSteps = 3;

		// translate to sphere center
		matrices.push();
		matrices.translate(origin.x, origin.y, origin.z);

		// grab matrices used by VertexConsumer
		Matrix4f modelMat = matrices.peek().getPositionMatrix();
		Matrix3f normalMat = matrices.peek().getNormalMatrix();

		final BufferBuilder buffer = SevenElementsRenderer.createBuffer(allocator, SevenElementsRenderPipelines.SPHERE);

		for (int lat = 0; lat < latSteps; lat++) {
			final double theta1 = Math.PI * lat / (double) latSteps;
			final double theta2 = Math.PI * (lat + 1) / (double) latSteps;

			for (int lon = 0; lon < lonSteps; lon++) {
				final double phi1 = 2.0 * Math.PI * lon / (double) lonSteps;
				final double phi2 = 2.0 * Math.PI * (lon + 1) / (double) lonSteps;

				final Vec3d v00 = spherical(radius, theta1, phi1);
				final Vec3d v01 = spherical(radius, theta1, phi2);
				final Vec3d v10 = spherical(radius, theta2, phi1);
				final Vec3d v11 = spherical(radius, theta2, phi2);

				vertex(buffer, modelMat, normalMat, v10, colorFunc.apply(SphereRenderer.relativeClamp(v10, radius)));
				vertex(buffer, modelMat, normalMat, v00, colorFunc.apply(SphereRenderer.relativeClamp(v00, radius)));
				vertex(buffer, modelMat, normalMat, v11, colorFunc.apply(SphereRenderer.relativeClamp(v11, radius)));

				vertex(buffer, modelMat, normalMat, v00, colorFunc.apply(SphereRenderer.relativeClamp(v00, radius)));
				vertex(buffer, modelMat, normalMat, v11, colorFunc.apply(SphereRenderer.relativeClamp(v11, radius)));
				vertex(buffer, modelMat, normalMat, v01, colorFunc.apply(SphereRenderer.relativeClamp(v01, radius)));
			}
		}

		SevenElementsRenderLayer.getSphere().draw(buffer.end());

		matrices.pop();
	}

	private static Vec3d spherical(double r, double theta, double phi) {
		double x = r * Math.sin(theta) * Math.cos(phi);
		double y = r * Math.cos(theta);
		double z = r * Math.sin(theta) * Math.sin(phi);
		return new Vec3d(x, y, z);
	}

	private static void vertex(BufferBuilder buffer, Matrix4f projMat, Matrix3f normalMat, Vec3d pos, int color) {
		buffer
			.vertex(projMat, (float)pos.x, (float)pos.y, (float)pos.z)
			.color(color);
	}

	private static Vec3d relativeClamp(Vec3d pos, float radius) {
		final Vec3d relPos = pos.multiply(1 / radius);

		return new Vec3d(
			MathHelper.clamp(relPos.x, -1, 1),
			MathHelper.clamp(relPos.y, -1, 1),
			MathHelper.clamp(relPos.z, -1, 1)
		);
	}
}
