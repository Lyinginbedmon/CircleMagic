package com.lying.circles.data;

import org.jetbrains.annotations.Nullable;

import com.lying.circles.reference.Reference;
import com.lying.circles.world.CMWorldEvents;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.data.ExistingFileHelper;

public class CMPlacedFeatureTagProvider extends TagsProvider<PlacedFeature>
{
	public static TagKey<PlacedFeature> STATUES = TagKey.create(Registry.PLACED_FEATURE_REGISTRY, new ResourceLocation(Reference.ModInfo.MOD_ID, "old_statues"));
	
	public CMPlacedFeatureTagProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(generator, BuiltinRegistries.PLACED_FEATURE, Reference.ModInfo.MOD_ID, existingFileHelper);
	}
	
	public String getName() { return "Circle Magic worldgen feature tags"; }
	
	protected void addTags()
	{
		this.tag(STATUES).add(CMWorldEvents.OLD_STATUE.get());
	}
}
