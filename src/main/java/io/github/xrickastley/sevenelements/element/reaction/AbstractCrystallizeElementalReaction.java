package io.github.xrickastley.sevenelements.element.reaction;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.entity.CrystallizeShardEntity;
import io.github.xrickastley.sevenelements.entity.SevenElementsEntityTypes;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.MathHelper2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public abstract sealed class AbstractCrystallizeElementalReaction
	extends ElementalReaction
	permits PyroCrystallizeElementalReaction, HydroCrystallizeElementalReaction, ElectroCrystallizeElementalReaction, CryoCrystallizeElementalReaction, FrozenCrystallizeElementalReaction
{
	private static final Set<Block> AIR_BLOCKS = Set.of(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR);

	private final Element shieldElement;

	AbstractCrystallizeElementalReaction(Settings settings) {
		this(settings.setType(Type.SHIELD), settings.getAuraElement());
	}

	AbstractCrystallizeElementalReaction(Settings settings, Element shieldElement) {
		super(
			settings
				.setReactionMultiplier(1.0)
		);

		this.shieldElement = shieldElement;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		if (!(entity.level() instanceof final ServerLevel world)) return;

		final Vec3 spawnPos = this.clampToGround(world, this.toAbsolutePos(entity, new Vec3(0, 0, 1)));
		final CrystallizeShardEntity crystallizeShard = new CrystallizeShardEntity(SevenElementsEntityTypes.CRYSTALLIZE_SHARD, world, this.shieldElement, this.getReactionStrength(origin, world), origin);

		crystallizeShard.setPos(spawnPos);
		world.tryAddFreshEntityWithPassengers(crystallizeShard);
	}

	// Taken from LookingPosArgument#toAbsolutePos
	private Vec3 toAbsolutePos(final LivingEntity entity, final Vec3 lookingPos) {
		final Vec2 vec2f = entity.getRotationVector();
		final Vec3 vec3d = entity.position();

		float f = Mth.cos((vec2f.y + 90.0F) * 0.017453292F);
		float g = Mth.sin((vec2f.y + 90.0F) * 0.017453292F);
		float h = Mth.cos(-vec2f.x * 0.017453292F);
		float i = Mth.sin(-vec2f.x * 0.017453292F);
		float j = Mth.cos((-vec2f.x + 90.0F) * 0.017453292F);
		float k = Mth.sin((-vec2f.x + 90.0F) * 0.017453292F);
		Vec3 vec3d2 = new Vec3(f * h, i, g * h);
		Vec3 vec3d3 = new Vec3(f * j, k, g * j);
		Vec3 vec3d4 = vec3d2.cross(vec3d3).scale(-1.0);
		double d = vec3d2.x * lookingPos.z + vec3d3.x * lookingPos.y + vec3d4.x * lookingPos.x;
		double e = vec3d2.y * lookingPos.z + vec3d3.y * lookingPos.y + vec3d4.y * lookingPos.x;
		double l = vec3d2.z * lookingPos.z + vec3d3.z * lookingPos.y + vec3d4.z * lookingPos.x;
		return new Vec3(vec3d.x + d, vec3d.y + e, vec3d.z + l);
	}

	private Vec3 clampToGround(Level world, Vec3 pos) {
		final BlockPos originPos = MathHelper2.asBlockPos(pos);
		final BlockState blockState = world.getBlockState(originPos);

		final Optional<BlockPos> blockPos = AIR_BLOCKS.contains(blockState.getBlock())
			? this.scan(world, originPos, new Vec3i(0, -1, 0), Functions.composePredicate(BlockState::getBlock, AIR_BLOCKS::contains), bp -> bp.getY() >= world.getMinY()).map(bp -> bp.offset(0, 1, 0))
			: this.scan(world, originPos, new Vec3i(0, +1, 0), Functions.composePredicate(BlockState::getBlock, Predicate.not(AIR_BLOCKS::contains)), bp -> bp.getY() <= world.getMaxY());

		final BlockPos finalBlockPos = blockPos.orElse(originPos);

		return new Vec3(pos.x, finalBlockPos.getY(), pos.z);
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
	private Optional<BlockPos> scan(final Level world, final BlockPos originPos, final Vec3i shift, final Predicate<BlockState> blockPredicate, final Predicate<BlockPos> posPredicate) {
		BlockPos blockPos = originPos;
		BlockState blockState = world.getBlockState(blockPos);

		while (posPredicate.test(blockPos)) {
			if (blockPredicate.test(blockState)) {
				blockPos = blockPos.offset(shift);
				blockState = world.getBlockState(blockPos);

				continue;
			}

			final int diff = originPos.getY() - blockPos.getY();

			return Optional.of(originPos.offset(0, -diff, 0));
		}

		return Optional.empty();
	}
}
