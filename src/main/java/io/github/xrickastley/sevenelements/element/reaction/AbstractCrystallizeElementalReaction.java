package io.github.xrickastley.sevenelements.element.reaction;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.entity.CrystallizeShardEntity;
import io.github.xrickastley.sevenelements.entity.SevenElementsEntityTypes;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.MathHelper2;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public abstract sealed class AbstractCrystallizeElementalReaction
	extends ElementalReaction
	permits PyroCrystallizeElementalReaction, HydroCrystallizeElementalReaction, ElectroCrystallizeElementalReaction, CryoCrystallizeElementalReaction, FrozenCrystallizeElementalReaction
{
	private static final Set<Block> AIR_BLOCKS = Set.of(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR);

	AbstractCrystallizeElementalReaction(Settings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final World world = entity.getWorld();

		if (!(world instanceof final ServerWorld serverWorld)) return;

		final Vec3d spawnPos = this.clampToGround(entity.getWorld(), this.toAbsolutePos(entity, new Vec3d(0, 0, 1)));
		final CrystallizeShardEntity crystallizeShard = new CrystallizeShardEntity(SevenElementsEntityTypes.CRYSTALLIZE_SHARD, serverWorld, this.getAuraElement(), origin);

		crystallizeShard.setPosition(spawnPos);
		serverWorld.spawnNewEntityAndPassengers(crystallizeShard);
	}

	// Taken from LookingPosArgument#toAbsolutePos
	private Vec3d toAbsolutePos(final LivingEntity entity, final Vec3d lookingPos) {
		final Vec2f vec2f = entity.getRotationClient();
		final Vec3d vec3d = entity.getPos();

		float f = MathHelper.cos((vec2f.y + 90.0F) * 0.017453292F);
		float g = MathHelper.sin((vec2f.y + 90.0F) * 0.017453292F);
		float h = MathHelper.cos(-vec2f.x * 0.017453292F);
		float i = MathHelper.sin(-vec2f.x * 0.017453292F);
		float j = MathHelper.cos((-vec2f.x + 90.0F) * 0.017453292F);
		float k = MathHelper.sin((-vec2f.x + 90.0F) * 0.017453292F);
		Vec3d vec3d2 = new Vec3d(f * h, i, g * h);
		Vec3d vec3d3 = new Vec3d(f * j, k, g * j);
		Vec3d vec3d4 = vec3d2.crossProduct(vec3d3).multiply(-1.0);
		double d = vec3d2.x * lookingPos.z + vec3d3.x * lookingPos.y + vec3d4.x * lookingPos.x;
		double e = vec3d2.y * lookingPos.z + vec3d3.y * lookingPos.y + vec3d4.y * lookingPos.x;
		double l = vec3d2.z * lookingPos.z + vec3d3.z * lookingPos.y + vec3d4.z * lookingPos.x;
		return new Vec3d(vec3d.x + d, vec3d.y + e, vec3d.z + l);
   	}

	private Vec3d clampToGround(World world, Vec3d pos) {
		final BlockPos originPos = MathHelper2.asBlockPos(pos);
		final BlockState blockState = world.getBlockState(originPos);

		final Optional<BlockPos> blockPos = AIR_BLOCKS.contains(blockState.getBlock())
			? this.scan(world, originPos, new Vec3i(0, -1, 0), Functions.composePredicate(BlockState::getBlock, AIR_BLOCKS::contains), bp -> bp.getY() >= world.getBottomY()).map(bp -> bp.add(0, 1, 0))
			: this.scan(world, originPos, new Vec3i(0, +1, 0), Functions.composePredicate(BlockState::getBlock, Predicate.not(AIR_BLOCKS::contains)), bp -> bp.getY() <= world.getTopYInclusive());

		final BlockPos finalBlockPos = blockPos.orElse(originPos);

		return new Vec3d(pos.x, finalBlockPos.getY(), pos.z);
	}

	/**
	 * Starting from the specified {@code initialBlockPos}, continuously scan by shfiting the
	 * {@code initialBlockPos} by {@code shift} until the {@code Block} at the shifted pos
	 * fulfills the {@code blockPredicate} or until {@code posPredicate} returns {@code false}. <br> <br>
	 *
	 * Returns either an {@code Optional} containing the {@code BlockPos} such that
	 * {@code blockPredicate.test(world.getBlockState(blockPos))} returns {@code false} or an empty
	 * {@code Optional} when {@code posPredicate.test(blockPos)} prematurely returns {@code false}.
	 *
	 * @param world The world.
	 * @param originPos The origin block pos.
	 * @param shift How much to shift by per iteration.
	 * @param blockPredicate The condition that must be fulfilled by the block at the specified position.
	 * @param posPredicate The condition that must be fulfilled for a next iteration to execute.
	 */
	private Optional<BlockPos> scan(final World world, final BlockPos originPos, final Vec3i shift, final Predicate<BlockState> blockPredicate, final Predicate<BlockPos> posPredicate) {
		BlockPos blockPos = originPos;
		BlockState blockState = world.getBlockState(blockPos);

		while (posPredicate.test(blockPos)) {
			if (blockPredicate.test(blockState)) {
				blockPos = blockPos.add(shift);
				blockState = world.getBlockState(blockPos);

				continue;
			}

			final int diff = originPos.getY() - blockPos.getY();

			return Optional.of(originPos.add(0, -diff, 0));
		}

		return Optional.empty();
	}
}
