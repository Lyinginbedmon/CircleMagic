package com.lying.misc19.blocks;

import com.lying.misc19.world.GenericTreeHolder;
import com.lying.misc19.world.M19WorldEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class MagicSapling extends SaplingBlock
{
	private static Direction GROW_FACE = Direction.NORTH;
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	private final AbstractTreeGrower treeGrower;
	
	public MagicSapling(Properties propertiesIn)
	{
		super(new GenericTreeHolder(() -> M19WorldEvents.MAGIC_TREE), propertiesIn);
		this.treeGrower = new GenericTreeHolder(() -> M19WorldEvents.MAGIC_TREE);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
	}
	
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}
	
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> context)
	{
		super.createBlockStateDefinition(context);
		context.add(FACING);
	}
	
	/** World events can't reliably find block entities, so we use this as a pass-through */
	public static Direction getLatestFacing() { return GROW_FACE; }
	
	public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random)
	{
		if (state.getValue(STAGE) == 0) {
			level.setBlock(pos, state.cycle(STAGE), 4);
		} else {
			if (!net.minecraftforge.event.ForgeEventFactory.saplingGrowTree(level, random, pos)) return;
			GROW_FACE = state.getValue(FACING);
			this.treeGrower.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
		}
	}
}
