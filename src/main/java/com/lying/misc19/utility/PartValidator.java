package com.lying.misc19.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface PartValidator
{
	public boolean validate(BlockPos pos, BlockState state, Level world, BlockPos cruciblePos);
}
