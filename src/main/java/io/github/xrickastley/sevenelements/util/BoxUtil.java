package io.github.xrickastley.sevenelements.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BoxUtil {
	public static AABB multiplyBox(AABB box, double factor) {
		final double centerX = (box.minX + box.maxX) / 2;
		final double centerY = (box.minY + box.maxY) / 2;
		final double centerZ = (box.minZ + box.maxZ) / 2;

		final double halfExtentX = ((box.maxX - box.minX) / 2) * factor;
		final double halfExtentY = ((box.maxY - box.minY) / 2) * factor;
		final double halfExtentZ = ((box.maxZ - box.minZ) / 2) * factor;

		return new AABB(centerX - halfExtentX, centerY - halfExtentY, centerZ - halfExtentZ, centerX + halfExtentX, centerY + halfExtentY, centerZ + halfExtentZ);
	}

	public static boolean isColliding(AABB box, AABB other) {
		return box.minX <= other.maxX && box.maxX >= other.minX
			&& box.minY <= other.maxY && box.maxY >= other.minY
			&& box.minZ <= other.maxZ && box.maxZ >= other.minZ;
	}

	public static Vec3 randomPos(final AABB box) {
		final RandomSource RANDOM = RandomSource.create();

		return new Vec3(
			Mth.nextDouble(RANDOM, box.minX, box.maxX),
			Mth.nextDouble(RANDOM, box.minY, box.maxY),
			Mth.nextDouble(RANDOM, box.minZ, box.maxZ)
		);
	}
}
