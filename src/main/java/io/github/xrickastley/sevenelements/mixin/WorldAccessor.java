package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.particle.BlockParticleEffect;
import net.minecraft.util.collection.Pool;
import net.minecraft.world.World;

@Mixin(World.class)
public interface WorldAccessor {
	@Accessor("EXPLOSION_BLOCK_PARTICLES")
	public static Pool<BlockParticleEffect> getExplosionBlockParticles() {
		throw new AssertionError();
	}
}
