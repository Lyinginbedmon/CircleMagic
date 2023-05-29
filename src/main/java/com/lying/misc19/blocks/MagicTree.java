package com.lying.misc19.blocks;

import javax.annotation.Nullable;

import com.lying.misc19.blocks.entity.MagicTreeBlockEntity;
import com.lying.misc19.init.M19BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MagicTree extends DoublePlantBlock implements ICruciblePart, EntityBlock
{
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE_Z = Block.box(0, 0, 4, 16, 16, 12);
	private static final VoxelShape SHAPE_X = Block.box(4, 0, 0, 12, 16, 16);
	
	public MagicTree(Properties propertiesIn)
	{
		super(propertiesIn);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
	}
	
	public PartType partType(BlockPos pos, BlockState state, Level world) { return PartType.BOUGH; }
	
	public boolean isMainHalf(BlockState state) { return state.getValue(HALF) == DoubleBlockHalf.LOWER; }
	
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return state.getValue(FACING).getAxis() == Axis.X ? SHAPE_X : SHAPE_Z; }
	
	public RenderShape getRenderShape(BlockState state)
	{
		return isMainHalf(state) ? RenderShape.MODEL : RenderShape.INVISIBLE;
	}
	
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
	{
		if(isMainHalf(state))
			return true;
		else
			return super.canSurvive(state, world, pos);
	}
	
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if(isMainHalf(state))
			return super.use(state, world, pos, player, hand, hitResult);
		else
		{
			BlockPos posBelow = pos.below();
			BlockState stateBelow = world.getBlockState(posBelow);
			return super.use(stateBelow, world, posBelow, player, hand, hitResult);
		}
	}
	
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return isMainHalf(state) ? new MagicTreeBlockEntity(pos, state) : null;
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
	{
		return createTickerHelper(type, M19BlockEntities.MAGIC_TREE.get(), world.isClientSide() ? MagicTreeBlockEntity::tickClient : MagicTreeBlockEntity::tickServer);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_)
	{
		return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
	}
	
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> context)
	{
		super.createBlockStateDefinition(context);
		context.add(FACING);
	}
}
