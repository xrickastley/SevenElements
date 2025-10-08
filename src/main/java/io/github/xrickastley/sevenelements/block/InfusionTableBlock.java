package io.github.xrickastley.sevenelements.block;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;
import io.github.xrickastley.sevenelements.screen.ElementalInfusionScreenHandler;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Colors;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public final class InfusionTableBlock extends HorizontalFacingBlock {
	public static final MapCodec<InfusionTableBlock> CODEC = createCodec(InfusionTableBlock::new);
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
	private static final VoxelShape LOWER;
	private static final VoxelShape UPPER;
	private static final VoxelShape SHAPE;

	InfusionTableBlock() {
		this(AbstractBlock.Settings.create());
	}

	private InfusionTableBlock(AbstractBlock.Settings settings) {
		super(
			settings
				.requiresTool()
				.strength(3, 4)
				.pistonBehavior(PistonBehavior.BLOCK)
		);

		this.setDefaultState(
			this.stateManager.getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(HALF, DoubleBlockHalf.LOWER)
		);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return super.getPlacementState(ctx)
			.with(FACING, ctx.getHorizontalPlayerFacing());
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		final BlockPos blockPos = pos.up();

		world.setBlockState(blockPos, this.getDefaultState().with(HALF, DoubleBlockHalf.UPPER));
	}

	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (state.get(HALF) == DoubleBlockHalf.LOWER && state.getBlock() == this) {
			if (world.getBlockState(pos.up()).getBlock() == this) world.removeBlock(pos.up(), false);
		} else if (state.get(HALF) == DoubleBlockHalf.UPPER && state.getBlock() == this) {
			if (world.getBlockState(pos.down()).getBlock() == this) world.removeBlock(pos.down(), false);
		}

		return super.onBreak(world, pos, state, player);
	}

	@Override
	protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		super.appendProperties(builder);

		builder.add(FACING, HALF);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) return ActionResult.SUCCESS;

		if (world.getGameRules().getBoolean(SevenElementsGameRules.INFUSION_TABLE)) {
			player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
		} else  {
			player.sendMessage(
				Text.translatable("container.seven-elements.infusion_table.fail_by_gamerule").withColor(Colors.LIGHT_RED)
			);
		}

		return ActionResult.CONSUME;
	}

	@Override
	protected @Nullable NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
		return new SimpleNamedScreenHandlerFactory(
			(syncId, inventory, player) -> new ElementalInfusionScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)),
			Text.translatable("container.seven-elements.infusion_table")
		);
	}

	@Override
	protected boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(HALF) == DoubleBlockHalf.UPPER
			? UPPER.offset(0, -1, 0)
			: LOWER;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(HALF) == DoubleBlockHalf.UPPER
			? world.getBlockState(pos.down()).getBlock() != this
				? UPPER.offset(0, -1, 0)
				: VoxelShapes.empty()
			: world.getBlockState(pos.up()).getBlock() != this
				? LOWER
				: SHAPE;
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		if (state.get(HALF) != DoubleBlockHalf.UPPER)
			return super.canPlaceAt(state, world, pos);

		final BlockState blockState = world.getBlockState(pos.down());

		return blockState.isOf(this) && blockState.get(HALF) == DoubleBlockHalf.LOWER;
	}

	static {
		LOWER = VoxelShapes.union(
			Block.createCuboidShape(0, 0, 0, 16, 2, 16),
			VoxelShapes.union(
				Block.createCuboidShape(3, 2, 3, 13, 4, 13),
				Block.createCuboidShape(2, 2, 2, 4, 4, 4),
				Block.createCuboidShape(12, 2, 2, 14, 4, 4),
				Block.createCuboidShape(2, 2, 14, 4, 4, 14),
				Block.createCuboidShape(12, 2, 12, 14, 4, 14)
			),
			Block.createCuboidShape(5, 4, 5, 11, 14, 11),
			Block.createCuboidShape(0, 14, 0, 16, 16, 16)
		);

		UPPER = VoxelShapes.union(
			Block.createCuboidShape(3, 16, 3, 13, 18, 13),
			Block.createCuboidShape(0, 18, 0, 16, 20, 16)
		);

		SHAPE = VoxelShapes.union(LOWER, UPPER);
	}
}
