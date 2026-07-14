package io.github.xrickastley.sevenelements.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * A class for explosions that don't damage entities.
 */
public class NonEntityDamagingExplosion implements Explosion {
	private static final ExplosionDamageCalculator DEFAULT_BEHAVIOR = new ExplosionDamageCalculator();
	private final boolean createFire;
	private final Explosion.BlockInteraction destructionType;
	private final ServerLevel world;
	private final Vec3 pos;
	private final @Nullable Entity entity;
	private final float power;
	private final ExplosionDamageCalculator behavior;
	private final List<Entity> affectedEntities = new ArrayList<>();
	private final Map<Player, Vec3> knockbackByPlayer = new HashMap<>();

	public NonEntityDamagingExplosion(ServerLevel world, @Nullable Entity entity, @Nullable ExplosionDamageCalculator behavior, Vec3 pos, float power, boolean createFire, Explosion.BlockInteraction destructionType) {
		this.world = world;
		this.entity = entity;
		this.power = power;
		this.pos = pos;
		this.createFire = createFire;
		this.destructionType = destructionType;
		this.behavior = behavior == null ? this.makeBehavior(entity) : behavior;
	}

	private ExplosionDamageCalculator makeBehavior(@Nullable Entity entity) {
		return (ExplosionDamageCalculator)(entity == null ? DEFAULT_BEHAVIOR : new EntityBasedExplosionDamageCalculator(entity));
	}

	public static float calculateReceivedDamage(Vec3 pos, Entity entity) {
		AABB box = entity.getBoundingBox();
		double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
		double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
		double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
		double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
		double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
		if (!(d < 0.0) && !(e < 0.0) && !(f < 0.0)) {
			int i = 0;
			int j = 0;

			for(double k = 0.0; k <= 1.0; k += d) {
				for(double l = 0.0; l <= 1.0; l += e) {
					for(double m = 0.0; m <= 1.0; m += f) {
						double n = Mth.lerp(k, box.minX, box.maxX);
						double o = Mth.lerp(l, box.minY, box.maxY);
						double p = Mth.lerp(m, box.minZ, box.maxZ);
						Vec3 vec3d = new Vec3(n + g, o, p + h);
						if (entity.level().clip(new ClipContext(vec3d, pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS) {
							++i;
						}

						++j;
					}
				}
			}

			return (float)i / (float)j;
		} else {
			return 0.0F;
		}
	}

	public float radius() {
		return this.power;
	}

	public Vec3 center() {
		return this.pos;
	}

	@SuppressWarnings("unused")
	private List<BlockPos> getBlocksToDestroy() {
		Set<BlockPos> set = new HashSet<>();

		for(int j = 0; j < 16; ++j) {
			for(int k = 0; k < 16; ++k) {
				for(int l = 0; l < 16; ++l) {
					if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
						double d = (double)((float)j / 15.0F * 2.0F - 1.0F);
						double e = (double)((float)k / 15.0F * 2.0F - 1.0F);
						double f = (double)((float)l / 15.0F * 2.0F - 1.0F);
						double g = Math.sqrt(d * d + e * e + f * f);
						d /= g;
						e /= g;
						f /= g;
						float h = this.power * (0.7F + this.world.getRandom().nextFloat() * 0.6F);
						double m = this.pos.x;
						double n = this.pos.y;
						double o = this.pos.z;

						for(float p = 0.3F; h > 0.0F; h -= 0.22500001F) {
							BlockPos blockPos = BlockPos.containing(m, n, o);
							BlockState blockState = this.world.getBlockState(blockPos);
							FluidState fluidState = this.world.getFluidState(blockPos);
							if (!this.world.isInWorldBounds(blockPos)) {
								break;
							}

							Optional<Float> optional = this.behavior.getBlockExplosionResistance(this, this.world, blockPos, blockState, fluidState);
							if (optional.isPresent()) {
								h -= ((Float)optional.get() + 0.3F) * 0.3F;
							}

							if (h > 0.0F && this.behavior.shouldBlockExplode(this, this.world, blockPos, blockState, h)) {
								set.add(blockPos);
							}

							m += d * 0.30000001192092896;
							n += e * 0.30000001192092896;
							o += f * 0.30000001192092896;
						}
					}
				}
			}
		}

		return new ObjectArrayList<>(set);
	}

	private void damageEntities() {
		float f = this.power * 2.0F;
		int i = Mth.floor(this.pos.x - (double)f - 1.0);
		int j = Mth.floor(this.pos.x + (double)f + 1.0);
		int k = Mth.floor(this.pos.y - (double)f - 1.0);
		int l = Mth.floor(this.pos.y + (double)f + 1.0);
		int m = Mth.floor(this.pos.z - (double)f - 1.0);
		int n = Mth.floor(this.pos.z + (double)f + 1.0);
		List<Entity> list = this.world.getEntities(this.entity, new AABB((double)i, (double)k, (double)m, (double)j, (double)l, (double)n));
		Iterator<Entity> var9 = list.iterator();

		while (true) {
			Entity entity;
			double d, e, g, h, o;

			do {
				do {
					do {
						if (!var9.hasNext()) return;

						entity = var9.next();
					} while (entity.ignoreExplosion(this));

					d = Math.sqrt(entity.distanceToSqr(this.pos)) / (double) f;
				} while (!(d <= 1.0));

				e = entity.getX() - this.pos.x;
				g = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.pos.y;
				h = entity.getZ() - this.pos.z;
				o = Math.sqrt(e * e + g * g + h * h);
			} while (o == 0.0);

			e /= o;
			g /= o;
			h /= o;
			boolean bl = this.behavior.shouldDamageEntity(this, entity);
			float p = this.behavior.getKnockbackMultiplier(entity);
			float q = !bl && p == 0.0F ? 0.0F : calculateReceivedDamage(this.pos, entity);



			double r = (1.0 - d) * (double) q * (double) p;
			double s = entity instanceof LivingEntity livingEntity
				? r * (1.0 - livingEntity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE))
				: r;

			e *= s;
			g *= s;
			h *= s;
			Vec3 vec3d = new Vec3(e, g, h);
			entity.setDeltaMovement(entity.getDeltaMovement().add(vec3d));
			if (entity instanceof Player playerEntity) {
				if (!playerEntity.isSpectator() && (!playerEntity.isCreative() || !playerEntity.getAbilities().flying)) {
					this.knockbackByPlayer.put(playerEntity, vec3d);
				}
			}

			entity.onExplosionHit(this.entity);
			this.affectedEntities.add(entity);
		}
	}

	private void destroyBlocks(List<BlockPos> positions) {
		List<DroppedItem> list = new ArrayList<>();
		Util.shuffle(positions, this.world.getRandom());
		Iterator<BlockPos> posIterator = positions.iterator();

		while(posIterator.hasNext()) {
			BlockPos blockPos = (BlockPos)posIterator.next();
			this.world.getBlockState(blockPos).onExplosionHit(this.world, blockPos, this, (item, pos) -> {
				addDroppedItem(list, item, pos);
			});
		}

		Iterator<DroppedItem> droppedItemIterator = list.iterator();

		while(droppedItemIterator.hasNext()) {
			final DroppedItem droppedItem = droppedItemIterator.next();
			Block.popResource(this.world, droppedItem.pos, droppedItem.item);
		}

	}

	private void createFire(List<BlockPos> positions) {
		Iterator<BlockPos> var2 = positions.iterator();

		while(var2.hasNext()) {
			BlockPos blockPos = (BlockPos)var2.next();
			if (this.world.getRandom().nextInt(3) == 0 && this.world.getBlockState(blockPos).isAir() && this.world.getBlockState(blockPos.below()).isSolidRender()) {
				this.world.setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.world, blockPos));
			}
		}

	}

	public void explode() {
		this.world.gameEvent(this.entity, GameEvent.EXPLODE, this.pos);
		List<BlockPos> list = this.getBlocksToDestroy();
		this.damageEntities();
		if (this.shouldDestroyBlocks()) {
			ProfilerFiller profiler = Profiler.get();
			profiler.push("explosion_blocks");
			this.destroyBlocks(list);
			profiler.pop();
		}

		if (this.createFire) {
			this.createFire(list);
		}

	}

	private static void addDroppedItem(List<DroppedItem> droppedItemsOut, ItemStack item, BlockPos pos) {
		Iterator<DroppedItem> var3 = droppedItemsOut.iterator();

		do {
			if (!var3.hasNext()) {
				droppedItemsOut.add(new DroppedItem(pos, item));
				return;
			}

			DroppedItem droppedItem = (DroppedItem)var3.next();
			droppedItem.merge(item);
		} while(!item.isEmpty());

	}

	private boolean shouldDestroyBlocks() {
		return this.destructionType != BlockInteraction.KEEP;
	}

	public Map<Player, Vec3> getKnockbackByPlayer() {
		return this.knockbackByPlayer;
	}

	public ServerLevel level() {
		return this.world;
	}

	public @Nullable LivingEntity getIndirectSourceEntity() {
		return Explosion.getIndirectSourceEntity(this.entity);
	}

	public @Nullable Entity getDirectSourceEntity() {
		return this.entity;
	}

	public Explosion.BlockInteraction getBlockInteraction() {
		return this.destructionType;
	}

	public List<Entity> getAffectedEntities() {
		return Collections.unmodifiableList(affectedEntities);
	}

	public boolean canTriggerBlocks() {
		if (this.destructionType != BlockInteraction.TRIGGER_BLOCK) {
			return false;
		} else {
			return this.entity != null && this.entity.getType() == EntityTypes.BREEZE_WIND_CHARGE ? this.world.getGameRules().get(GameRules.MOB_GRIEFING) : true;
		}
	}

	public boolean shouldAffectBlocklikeEntities() {
		boolean bl = this.world.getGameRules().get(GameRules.MOB_GRIEFING);
		boolean bl2 = this.entity == null || !this.entity.isInWater();
		boolean bl3 = this.entity == null || this.entity.getType() != EntityTypes.BREEZE_WIND_CHARGE && this.entity.getType() != EntityTypes.WIND_CHARGE;
		if (bl) {
			return bl2 && bl3;
		} else {
			return this.destructionType.shouldAffectBlocklikeEntities() && bl2 && bl3;
		}
	}

	public boolean isSmall() {
		return this.power < 2.0F || !this.shouldDestroyBlocks();
	}

	static class DroppedItem {
		final BlockPos pos;
		ItemStack item;

		DroppedItem(BlockPos pos, ItemStack item) {
			this.pos = pos;
			this.item = item;
		}

		public void merge(ItemStack other) {
			if (ItemEntity.areMergable(this.item, other)) {
				this.item = ItemEntity.merge(this.item, other, 16);
			}
		}
	}
}
