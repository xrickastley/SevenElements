package io.github.xrickastley.sevenelements.util;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.mixin.ExplosionAccessor;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * A class for explosions that don't damage entities.
 */
public class NonEntityDamagingExplosion extends Explosion {
	private World world;
	private double x;
	private double y;
	private double z;
	private @Nullable Entity entity;
	private float power;
	private ExplosionBehavior behavior;
	private ObjectArrayList<BlockPos> affectedBlocks;
	private Map<PlayerEntity, Vec3d> affectedPlayers;
	private final List<Entity> affectedEntities = new ArrayList<>();

	public NonEntityDamagingExplosion(final World world, final @Nullable Entity entity, final double x, final double y, final double z, final float power, final List<BlockPos> affectedBlocks) {
		super(world, entity, x, y, z, power, false, Explosion.DestructionType.DESTROY_WITH_DECAY, affectedBlocks);

		supplyFromAccessor();
	}

	public NonEntityDamagingExplosion(final World world, final @Nullable Entity entity, final double x, final double y, final double z, final float power, final boolean createFire, final Explosion.DestructionType destructionType, final List<BlockPos> affectedBlocks) {
		super(world, entity, x, y, z, power, createFire, destructionType, affectedBlocks);

		supplyFromAccessor();
	}

	public NonEntityDamagingExplosion(final World world, final @Nullable Entity entity, final double x, final double y, final double z, final float power, final boolean createFire, final Explosion.DestructionType destructionType) {
		super(world, entity, x, y, z, power, createFire, destructionType);

		supplyFromAccessor();
	}

	public NonEntityDamagingExplosion(final World world, final @Nullable Entity entity, final @Nullable ExplosionBehavior behavior, final double x, final double y, final double z, final float power, final boolean createFire, final Explosion.DestructionType destructionType) {
		super(world, entity, null, behavior, x, y, z, power, createFire, destructionType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.ENTITY_GENERIC_EXPLODE);

		supplyFromAccessor();
	}

	/**
	 * Another version of {@link Explosion#collectBlocksAndDamageEntities}, but pushes
	 * entities (applies velocity) instead of damaging them.
	 */
	public void collectBlocksAndPushEntities() {
		this.world.emitGameEvent(this.entity, GameEvent.EXPLODE, new Vec3d(this.x, this.y, this.z));
		final Set<BlockPos> set = Sets.newHashSet();

		for (int j = 0; j < 16; ++j) {
			for (int k = 0; k < 16; ++k) {
				for (int l = 0; l < 16; ++l) {
					if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
						double d = j / 15.0f * 2.0f - 1.0f;
						double e = k / 15.0f * 2.0f - 1.0f;
						double f = l / 15.0f * 2.0f - 1.0f;
						final double g = Math.sqrt(d * d + e * e + f * f);

						d /= g;
						e /= g;
						f /= g;

						float h = this.power * (0.7f + this.world.random.nextFloat() * 0.6f);
						double m = this.x;
						double n = this.y;
						double o = this.z;

						while (h > 0.0f) {
							final BlockPos blockPos = BlockPos.ofFloored(m, n, o);
							final BlockState blockState = this.world.getBlockState(blockPos);
							final FluidState fluidState = this.world.getFluidState(blockPos);

							if (!this.world.isInBuildLimit(blockPos)) break;

							final Optional<Float> optional = this.behavior.getBlastResistance(this, this.world, blockPos, blockState, fluidState);

							if (optional.isPresent()) h -= (optional.get() + 0.3f) * 0.3f;

							if (h > 0.0f && this.behavior.canDestroyBlock(this, this.world, blockPos, blockState, h)) {
								set.add(blockPos);
							}
							m += d * 0.30000001192092896;
							n += e * 0.30000001192092896;
							o += f * 0.30000001192092896;
							h -= 0.22500001f;
						}
					}
				}
			}
		}

		this.affectedBlocks.addAll(set);

		final float q = this.power * 2.0f;
		int k = MathHelper.floor(this.x - q - 1.0);
		int l = MathHelper.floor(this.x + q + 1.0);
		final int r = MathHelper.floor(this.y - q - 1.0);
		final int s = MathHelper.floor(this.y + q + 1.0);
		final int t = MathHelper.floor(this.z - q - 1.0);
		final int u = MathHelper.floor(this.z + q + 1.0);

		final List<Entity> list = this.world.getOtherEntities(this.entity, new Box(k, r, t, l, s, u));
		final Vec3d vec3d = new Vec3d(this.x, this.y, this.z);

		for (final Entity entity : list) {
			if (entity.isImmuneToExplosion(this)) continue;

			final double v = Math.sqrt(entity.squaredDistanceTo(vec3d)) / q;

			if (v > 1.0) continue;

			double w = entity.getX() - this.x;
			double x = ((entity instanceof TntEntity) ? entity.getY() : entity.getEyeY()) - this.y;
			double y = entity.getZ() - this.z;
			final double z = Math.sqrt(w * w + x * x + y * y);

			if (z == 0.0) continue;

			w /= z;
			x /= z;
			y /= z;

			final double aa = (1.0 - v) * getExposure(vec3d, entity) * this.behavior.getKnockbackModifier(entity);
			final double ab = entity instanceof LivingEntity livingEntity
				? aa * (1.0 - livingEntity.getAttributeValue(EntityAttributes.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE))
				: aa;

			this.affectedEntities.add(entity);

			w *= ab;
			x *= ab;
			y *= ab;

			final Vec3d vec3d2 = new Vec3d(w, x, y);

			entity.setVelocity(entity.getVelocity().add(vec3d2));

			if (!(entity instanceof final PlayerEntity playerEntity)) continue;

			if (playerEntity.isSpectator() || (playerEntity.isCreative() && playerEntity.getAbilities().flying)) {
				continue;
			}

			this.affectedPlayers.put(playerEntity, vec3d2);
			entity.onExplodedBy(this.entity);
		}
	}

	public List<Entity> getAffectedEntities() {
		return this.affectedEntities;
	}

	private void supplyFromAccessor() {
		final ExplosionAccessor accessor = (ExplosionAccessor) this;

		this.world = accessor.getWorld();
		this.x = accessor.getX();
		this.y = accessor.getY();
		this.z = accessor.getZ();
		this.entity = accessor.getEntity();
		this.power = accessor.getPower();
		this.behavior = accessor.getBehavior();
		this.affectedBlocks = accessor.getAffectedBlocks();
		this.affectedPlayers = accessor.getAffectedPlayers();
	}
}
