package io.github.xrickastley.sevenelements.block;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;
import io.github.xrickastley.sevenelements.screen.ElementalInfusionScreenHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.CommonColors;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class InfusionTableBlock extends HorizontalDirectionalBlock {
	public static final MapCodec<InfusionTableBlock> CODEC = simpleCodec(InfusionTableBlock::new);
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	private static final VoxelShape LOWER;
	private static final VoxelShape UPPER;
	private static final VoxelShape SHAPE;

	InfusionTableBlock() {
		this(
			BlockBehaviour.Properties.of()
				.setId(SevenElements.registryKey(Registries.BLOCK, "infusion_table"))
		);
	}

	private InfusionTableBlock(BlockBehaviour.Properties settings) {
		super(
			settings
				.requiresCorrectToolForDrops()
				.strength(3, 4)
				.pushReaction(PushReaction.BLOCK)
		);

		this.registerDefaultState(
			this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(HALF, DoubleBlockHalf.LOWER)
		);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return super.getStateForPlacement(ctx)
			.setValue(FACING, ctx.getHorizontalDirection());
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		final BlockPos blockPos = pos.above();

		world.setBlockAndUpdate(blockPos, this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER));
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		if (state.getValue(HALF) == DoubleBlockHalf.LOWER && state.getBlock() == this) {
			if (world.getBlockState(pos.above()).getBlock() == this) world.removeBlock(pos.above(), false);
		} else if (state.getValue(HALF) == DoubleBlockHalf.UPPER && state.getBlock() == this) {
			if (world.getBlockState(pos.below()).getBlock() == this) world.removeBlock(pos.below(), false);
		}

		return super.playerWillDestroy(world, pos, state, player);
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);

		builder.add(FACING, HALF);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(world instanceof final ServerLevel serverWorld)) return InteractionResult.SUCCESS;

		if (serverWorld.getGameRules().get(SevenElementsGameRules.INFUSION_TABLE)) {
			player.openMenu(state.getMenuProvider(world, pos));
		} else {
			player.sendOverlayMessage(
				Component.translatable("container.seven-elements.infusion_table.fail_by_gamerule").withColor(CommonColors.SOFT_RED)
			);
		}

		return InteractionResult.CONSUME;
	}

	@Override
	protected @Nullable MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
		return new SimpleMenuProvider(
			(syncId, inventory, player) -> new ElementalInfusionScreenHandler(syncId, inventory, ContainerLevelAccess.create(world, pos)),
			Component.translatable("container.seven-elements.infusion_table")
		);
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER
			? UPPER.move(0, -1, 0)
			: LOWER;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER
			? world.getBlockState(pos.below()).getBlock() != this
				? UPPER.move(0, -1, 0)
				: Shapes.empty()
			: world.getBlockState(pos.above()).getBlock() != this
				? LOWER
				: SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		if (state.getValue(HALF) != DoubleBlockHalf.UPPER)
			return super.canSurvive(state, world, pos);

		final BlockState blockState = world.getBlockState(pos.below());

		return blockState.is(this) && blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
	}

	static {
		LOWER = Shapes.or(
			Block.box(0, 0, 0, 16, 2, 16),
			Shapes.or(
				Block.box(3, 2, 3, 13, 4, 13),
				Block.box(2, 2, 2, 4, 4, 4),
				Block.box(12, 2, 2, 14, 4, 4),
				Block.box(2, 2, 14, 4, 4, 14),
				Block.box(12, 2, 12, 14, 4, 14)
			),
			Block.box(5, 4, 5, 11, 14, 11),
			Block.box(0, 14, 0, 16, 16, 16)
		);

		UPPER = Shapes.or(
			Block.box(3, 16, 3, 13, 18, 13),
			Block.box(0, 18, 0, 16, 20, 16)
		);

		SHAPE = Shapes.or(LOWER, UPPER);
	}
}
