package io.github.xrickastley.sevenelements.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.function.Function;

import org.joml.Matrix4f;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SphereRenderer {
	private SphereRenderer() {}

	/**
	 * Render a sphere centered at (x, y, z) in world coordinates.
	 *
	 * @param consumer The vertex consumer to use for drawing the sphere.
	 * @param matrices The current {@code MatrixStack}.
	 * @param origin The origin point.
	 * @param radius Sphere radius (in blocks)
	 * @param latSteps Vertical subdivisions
	 * @param lonSteps Horizontal subdivisions
	 * @param colorFunc A function taking in a {@code Vec3d} and returns an ARGB int {@code 0xAARRGGBB}
	 */
	public static void render(VertexConsumer consumer, PoseStack matrices, Vec3 origin, float radius, int latSteps, int lonSteps, Function<Vec3, Integer> colorFunc) {
		if (latSteps < 2) latSteps = 2;
		if (lonSteps < 3) lonSteps = 3;

		// translate to sphere center
		matrices.pushPose();
		matrices.translate(origin.x, origin.y, origin.z);

		render(consumer, matrices.last(), radius, latSteps, lonSteps, colorFunc);

		matrices.popPose();
	}

	/**
	 * Render a sphere.
	 *
	 * @param consumer The vertex consumer to use for drawing the sphere.
	 * @param matrices The current {@code MatrixStack}.
	 * @param origin The origin point.
	 * @param radius Sphere radius (in blocks)
	 * @param latSteps Vertical subdivisions
	 * @param lonSteps Horizontal subdivisions
	 * @param colorFunc A function taking in a {@code Vec3d} and returns an ARGB int {@code 0xAARRGGBB}
	 */
	public static void render(VertexConsumer consumer, PoseStack.Pose entry, float radius, int latSteps, int lonSteps, Function<Vec3, Integer> colorFunc) {
		if (latSteps < 2) latSteps = 2;
		if (lonSteps < 3) lonSteps = 3;

		// grab matrices used by VertexConsumer
		Matrix4f matrix = entry.pose();

		for (int lat = 0; lat < latSteps; lat++) {
			final double theta1 = Math.PI * lat / (double) latSteps;
			final double theta2 = Math.PI * (lat + 1) / (double) latSteps;

			for (int lon = 0; lon < lonSteps; lon++) {
				final double phi1 = 2.0 * Math.PI * lon / (double) lonSteps;
				final double phi2 = 2.0 * Math.PI * (lon + 1) / (double) lonSteps;

				final Vec3 v00 = spherical(radius, theta1, phi1);
				final Vec3 v01 = spherical(radius, theta1, phi2);
				final Vec3 v10 = spherical(radius, theta2, phi1);
				final Vec3 v11 = spherical(radius, theta2, phi2);

				vertex(consumer, matrix, v10, colorFunc.apply(SphereRenderer.relativeClamp(v10, radius)));
				vertex(consumer, matrix, v00, colorFunc.apply(SphereRenderer.relativeClamp(v00, radius)));
				vertex(consumer, matrix, v11, colorFunc.apply(SphereRenderer.relativeClamp(v11, radius)));

				vertex(consumer, matrix, v00, colorFunc.apply(SphereRenderer.relativeClamp(v00, radius)));
				vertex(consumer, matrix, v11, colorFunc.apply(SphereRenderer.relativeClamp(v11, radius)));
				vertex(consumer, matrix, v01, colorFunc.apply(SphereRenderer.relativeClamp(v01, radius)));
			}
		}
	}

	private static Vec3 spherical(double r, double theta, double phi) {
		double x = r * Math.sin(theta) * Math.cos(phi);
		double y = r * Math.cos(theta);
		double z = r * Math.sin(theta) * Math.sin(phi);
		return new Vec3(x, y, z);
	}

	private static void vertex(VertexConsumer consumer, Matrix4f matrices, Vec3 pos, int color) {
		consumer
			.addVertex(matrices, (float) pos.x, (float) pos.y, (float) pos.z)
			.setColor(color);
	}

	private static Vec3 relativeClamp(Vec3 pos, float radius) {
		final Vec3 relPos = pos.scale(1 / radius);

		return new Vec3(
			Mth.clamp(relPos.x, -1, 1),
			Mth.clamp(relPos.y, -1, 1),
			Mth.clamp(relPos.z, -1, 1)
		);
	}
}
