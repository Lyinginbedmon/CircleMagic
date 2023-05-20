package com.lying.misc19.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class ImbuedBlock extends Block
{
	public static final EnumProperty<End> SHAPE_PROPERTY = EnumProperty.create("position", End.class);
	
	public ImbuedBlock(Properties p_49795_)
	{
		super(p_49795_);
		this.registerDefaultState(this.defaultBlockState().setValue(SHAPE_PROPERTY, End.NONE));
	}
	
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_55408_) { p_55408_.add(SHAPE_PROPERTY); }
	
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return getStateForPos(context.getLevel(), context.getClickedPos(), this);
	}
	
	public BlockState updateShape(BlockState oldState, Direction direction, BlockState newState, LevelAccessor world, BlockPos pos, BlockPos updateFrom)
	{
		return getStateForPos(world, pos, this);
	}
	
	private BlockState getStateForPos(LevelAccessor world, BlockPos pos, Block block)
	{
		BlockPos above = pos.above();
		BlockPos below = pos.below();
		
		BlockState blockAbove = world.getBlockState(above);
		BlockState blockBelow = world.getBlockState(below);
		
		boolean connectBelow = blockBelow.is(this) || blockBelow.getBlock() instanceof InscribedBlock;
		boolean connectAbove = blockAbove.is(this) || blockAbove.getBlock() instanceof InscribedBlock;
		
		if(connectBelow && connectAbove)
			return defaultBlockState().setValue(SHAPE_PROPERTY, End.NONE);
		else if(!connectAbove && connectBelow)
			return defaultBlockState().setValue(SHAPE_PROPERTY, End.TOP);
		else
			return defaultBlockState().setValue(SHAPE_PROPERTY, End.BOTTOM);
	}
	
	public static enum End implements StringRepresentable
	{
		TOP,
		BOTTOM,
		NONE;
		
		public String toString() { return this.name().toLowerCase(); }
		
		public String getSerializedName() { return this.toString(); }
	}
}
