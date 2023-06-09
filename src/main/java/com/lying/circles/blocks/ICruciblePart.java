package com.lying.circles.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ICruciblePart
{
	public PartType partType(BlockPos pos, BlockState state, Level world);
	
	public default boolean isPartValidFor(BlockPos pos, BlockState state, Level world, BlockPos cruciblePos) { return true; }
	
	public default int glyphCapBonus(BlockPos pos, BlockState state, Level world, BlockPos cruciblePos) { return 0; }
	
	public default boolean canProvideSuggestions(BlockPos pos, BlockState state, Level world, BlockPos cruciblePos) { return false; }
	
	public default ItemStack getHeldItem(BlockPos pos, BlockState state, Level world, BlockPos cruciblePos) { return ItemStack.EMPTY; }
	
	public static enum PartType
	{
		PILLAR,
		BOUGH,
		FAIRY;
	}
}
