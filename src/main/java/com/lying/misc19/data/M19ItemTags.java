package com.lying.misc19.data;

import javax.annotation.Nullable;

import com.lying.misc19.init.M19Items;
import com.lying.misc19.reference.Reference;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

public class M19ItemTags extends ItemTagsProvider
{
    public static final TagKey<Item> MAGICAL = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(Reference.ModInfo.MOD_ID, "magical"));
    
	public M19ItemTags(DataGenerator dataGenerator, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(dataGenerator, new M19BlockTags(dataGenerator, existingFileHelper), Reference.ModInfo.MOD_ID, existingFileHelper);
	}
	
	public String getName() { return "Circle Magic item tags"; }
	
	protected void addTags()
	{
		tag(MAGICAL).add(
			M19Items.CRUCIBLE_ITEM.get(),
			M19Items.INSCRIBED_ACACIA_ITEM.get(),
			M19Items.INSCRIBED_BIRCH_ITEM.get(),
			M19Items.INSCRIBED_CRIMSON_ITEM.get(),
			M19Items.INSCRIBED_DARK_OAK_ITEM.get(),
			M19Items.INSCRIBED_JUNGLE_ITEM.get(),
			M19Items.INSCRIBED_MANGROVE_ITEM.get(),
			M19Items.INSCRIBED_OAK_ITEM.get(),
			M19Items.INSCRIBED_SPRUCE_ITEM.get(),
			M19Items.INSCRIBED_WARPED_ITEM.get(),
			M19Items.INSCRIBED_STONE_ITEM.get());
	}
}