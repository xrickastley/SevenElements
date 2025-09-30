package io.github.xrickastley.sevenelements.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@Mixin(Explosion.class)
public interface ExplosionAccessor {
	@Accessor("world")
	public World getWorld();
	@Accessor("x")
	public double getX();
	@Accessor("y")
	public double getY();
	@Accessor("z")
	public double getZ();
	@Accessor("entity")
	public Entity getEntity();
	@Accessor("power")
	public float getPower();
	@Accessor("behavior")
	public ExplosionBehavior getBehavior();
	@Accessor("affectedBlocks")
	public ObjectArrayList<BlockPos> getAffectedBlocks();
	@Accessor("affectedPlayers")
	public Map<PlayerEntity, Vec3d> getAffectedPlayers();
}
