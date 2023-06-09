package com.lying.circles.data;

import javax.annotation.Nullable;

import com.lying.circles.init.CMItems;
import com.lying.circles.reference.Reference;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

public class CMItemTags extends ItemTagsProvider
{
    public static final TagKey<Item> MAGICAL = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(Reference.ModInfo.MOD_ID, "magical"));
    
	public CMItemTags(DataGenerator dataGenerator, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(dataGenerator, new CMBlockTags(dataGenerator, existingFileHelper), Reference.ModInfo.MOD_ID, existingFileHelper);
	}
	
	public String getName() { return "Circle Magic item tags"; }
	
	protected void addTags()
	{
		tag(MAGICAL).add(
			CMItems.CRUCIBLE_ITEM.get(),
			CMItems.INSCRIBED_ACACIA_ITEM.get(),
			CMItems.INSCRIBED_BIRCH_ITEM.get(),
			CMItems.INSCRIBED_CRIMSON_ITEM.get(),
			CMItems.INSCRIBED_DARK_OAK_ITEM.get(),
			CMItems.INSCRIBED_JUNGLE_ITEM.get(),
			CMItems.INSCRIBED_MANGROVE_ITEM.get(),
			CMItems.INSCRIBED_OAK_ITEM.get(),
			CMItems.INSCRIBED_SPRUCE_ITEM.get(),
			CMItems.INSCRIBED_WARPED_ITEM.get(),
			CMItems.INSCRIBED_STONE_ITEM.get());
	}
}