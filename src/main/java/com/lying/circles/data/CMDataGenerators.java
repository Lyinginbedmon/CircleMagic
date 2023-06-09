package com.lying.circles.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

public class CMDataGenerators
{
	public static void onGatherData(GatherDataEvent event)
	{
		DataGenerator generator = event.getGenerator();
		ExistingFileHelper fileHelper = event.getExistingFileHelper();
		generator.addProvider(event.includeServer(), new CMBlockLootProvider(generator, fileHelper));
		generator.addProvider(event.includeServer(), new CMItemTags(generator, fileHelper));
		generator.addProvider(event.includeServer(), new CMFunctionRecipeProvider(generator, fileHelper));
	}
}
