package com.lying.misc19.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class SimpleDoubleBlock extends Block
{
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	
	public SimpleDoubleBlock(Properties p_49795_)
	{
		super(p_49795_);
		registerDefaultState(defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
	}
	
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateHolder)
	{
		stateHolder.add(HALF);
	}
	
	public boolean isMainHalf(BlockState state) { return state.getValue(HALF) == DoubleBlockHalf.LOWER; }
	
	public RenderShape getRenderShape(BlockState state) { return isMainHalf(state) ? RenderShape.MODEL : RenderShape.INVISIBLE; }
	
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		world.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), UPDATE_ALL);
	}
	
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
	{
		BlockPos posBelow = pos.below();
		BlockState stateBelow = world.getBlockState(posBelow);
		return isMainHalf(state) ? stateBelow.isFaceSturdy(world, posBelow, Direction.UP) : stateBelow.is(this);
	}
	
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor world, BlockPos pos, BlockPos neighbourPos)
	{
		if(isMainHalf(state))
			return world.getBlockState(pos.above()).is(this) ? state : Blocks.AIR.defaultBlockState();
		else
			return canSurvive(state, world, pos) ? state : Blocks.AIR.defaultBlockState();
	}
}
