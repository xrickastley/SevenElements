package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.Level;

@Mixin(Level.class)
public interface LevelAccessor {
	@Accessor("DEFAULT_EXPLOSION_BLOCK_PARTICLES")
	public static WeightedList<ExplosionParticleInfo> getExplosionBlockParticles() {
		throw new AssertionError();
	}
}
