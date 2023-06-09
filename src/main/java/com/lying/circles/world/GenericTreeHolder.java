package com.lying.circles.world;

import com.google.common.base.Supplier;

import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class GenericTreeHolder extends AbstractTreeGrower
{
	private final Supplier<Holder<ConfiguredFeature<TreeConfiguration, ?>>> treeFeature;
	
	public GenericTreeHolder(Supplier<Holder<ConfiguredFeature<TreeConfiguration, ?>>> featureIn)
	{
		this.treeFeature = featureIn;
	}
	
	protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean p_222911_)
	{
		return this.treeFeature.get();
	}
}
