package com.lying.circles.world;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.lying.circles.blocks.MagicSapling;
import com.lying.circles.blocks.MagicTree;
import com.lying.circles.init.CMBlocks;
import com.lying.circles.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageAttachment;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class CMWorldEvents
{
	public static Holder<ConfiguredFeature<TreeConfiguration, ?>> MAGIC_TREE = FeatureUtils.register(
			Reference.ModInfo.MOD_ID+":magic_tree", 
			Feature.TREE, 
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(CMBlocks.MAGIC_TREE.get()), 
					new TrunkPlacer(1, 0, 0)
					{
						protected TrunkPlacerType<?> type() { return TrunkPlacerType.STRAIGHT_TRUNK_PLACER; }
						
						public List<FoliageAttachment> placeTrunk(LevelSimulatedReader world, BiConsumer<BlockPos, BlockState> consumer, RandomSource random, int p_226160_, BlockPos pos, TreeConfiguration config)
						{
							Direction facing = MagicSapling.getLatestFacing();
							BlockState state = CMBlocks.MAGIC_TREE.get().defaultBlockState().setValue(MagicTree.FACING, facing);
							consumer.accept(pos, state.setValue(MagicTree.HALF, DoubleBlockHalf.LOWER));
							consumer.accept(pos.above(), state.setValue(MagicTree.HALF, DoubleBlockHalf.UPPER));
							return ImmutableList.of();
						}
					}, 
					BlockStateProvider.simple(Blocks.OAK_LEAVES), 
					new BlobFoliagePlacer(ConstantInt.of(1), ConstantInt.of(0), 0), 
					new TwoLayersFeatureSize(0, 0, 0)).build());
}
