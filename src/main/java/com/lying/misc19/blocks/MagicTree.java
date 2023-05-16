package com.lying.misc19.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MagicTree extends Block implements ICruciblePart
{
	public MagicTree(Properties p_49795_)
	{
		super(p_49795_);
	}
	
	public PartType partType(BlockPos pos, BlockState state, Level world) { return PartType.BOUGH; }
}
