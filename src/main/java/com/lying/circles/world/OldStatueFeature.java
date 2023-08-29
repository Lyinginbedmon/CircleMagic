package com.lying.circles.world;

import com.lying.circles.blocks.Statue;
import com.lying.circles.init.CMBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

public class OldStatueFeature extends Feature<NoneFeatureConfiguration>
{
	public OldStatueFeature()
	{
		super(NoneFeatureConfiguration.CODEC);
	}
	
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
	{
		WorldGenLevel level = context.level();
		BlockPos pos = context.origin();
		
		System.out.println("Placing statue at "+pos.toShortString());
		
		boolean isWater = level.getFluidState(pos).is(Fluids.WATER);
		level.setBlock(pos, CMBlocks.STATUE.get().defaultBlockState().setValue(Statue.HALF, DoubleBlockHalf.LOWER).setValue(Statue.WATERLOGGED, isWater), 2);
		
		isWater = level.getFluidState(pos.above()).is(Fluids.WATER);
		level.setBlock(pos.above(), CMBlocks.STATUE.get().defaultBlockState().setValue(Statue.HALF, DoubleBlockHalf.UPPER).setValue(Statue.WATERLOGGED, isWater), 2);
		
		return true;
	}
}
