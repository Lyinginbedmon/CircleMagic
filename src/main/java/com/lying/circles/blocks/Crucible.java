package com.lying.circles.blocks;

import javax.annotation.Nullable;

import com.lying.circles.blocks.entity.CrucibleBlockEntity;
import com.lying.circles.init.CMBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Crucible extends SimpleDoubleBlock implements EntityBlock, SimpleWaterloggedBlock
{
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final VoxelShape SHAPE_LOWER = Shapes.or(Block.box(0, 0, 0, 16, 6, 16), Block.box(2, 6, 2, 14, 16, 14));
	protected static final VoxelShape SHAPE_UPPER = Shapes.or(Block.box(2, 0, 2, 14, 9, 14), Block.box(4, 9, 4, 12, 16, 12));
	
	public Crucible(Properties propertiesIn)
	{
		super(propertiesIn);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)));
	}
	
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateHolder)
	{
		stateHolder.add(HALF, WATERLOGGED);
	}
	
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return isMainHalf(state) ? SHAPE_LOWER : SHAPE_UPPER; }
	
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		BlockEntity blockEntity = world.getBlockEntity(isMainHalf(state) ? pos : pos.below());
		if(blockEntity.getType() == CMBlockEntities.CRUCIBLE.get())
			((CrucibleBlockEntity)blockEntity).openEditorFor(player);
		
		return InteractionResult.CONSUME;
	}
	
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) { return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state); }
	

	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor world, BlockPos pos, BlockPos neighbourPos)
	{
		if(state.getValue(WATERLOGGED))
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		
		return super.updateShape(state, direction, neighbour, world, pos, neighbourPos);
	}
	
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return isMainHalf(state) ? new CrucibleBlockEntity(pos, state) : null;
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
	{
		return createTickerHelper(type, CMBlockEntities.CRUCIBLE.get(), world.isClientSide() ? CrucibleBlockEntity::tickClient : CrucibleBlockEntity::tickServer);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_)
	{
		return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
	}
}
