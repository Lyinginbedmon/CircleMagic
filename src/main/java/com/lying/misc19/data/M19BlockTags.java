package com.lying.misc19.data;

import javax.annotation.Nullable;

import com.lying.misc19.init.M19Blocks;
import com.lying.misc19.reference.Reference;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

public class M19BlockTags extends BlockTagsProvider
{
    public static final TagKey<Block> MAGICAL = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(Reference.ModInfo.MOD_ID, "magical"));
	
	public M19BlockTags(DataGenerator p_126511_, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(p_126511_, Reference.ModInfo.MOD_ID, existingFileHelper);
	}
	
	public String getName() { return "Circle Magic block tags"; }
	
	protected void addTags()
	{
		tag(MAGICAL).add(
				M19Blocks.CRUCIBLE.get(),
				M19Blocks.INSCRIBED_ACACIA.get(),
				M19Blocks.INSCRIBED_BIRCH.get(),
				M19Blocks.INSCRIBED_CRIMSON.get(),
				M19Blocks.INSCRIBED_DARK_OAK.get(),
				M19Blocks.INSCRIBED_JUNGLE.get(),
				M19Blocks.INSCRIBED_MANGROVE.get(),
				M19Blocks.INSCRIBED_OAK.get(),
				M19Blocks.INSCRIBED_SPRUCE.get(),
				M19Blocks.INSCRIBED_WARPED.get(),
				M19Blocks.INSCRIBED_STONE.get());
		
		tag(BlockTags.MINEABLE_WITH_AXE).add(
				M19Blocks.INSCRIBED_ACACIA.get(), M19Blocks.IMBUED_ACACIA.get(), 
				M19Blocks.INSCRIBED_BIRCH.get(), M19Blocks.IMBUED_BIRCH.get(), 
				M19Blocks.INSCRIBED_CRIMSON.get(), M19Blocks.IMBUED_CRIMSON.get(), 
				M19Blocks.INSCRIBED_DARK_OAK.get(), M19Blocks.IMBUED_DARK_OAK.get(), 
				M19Blocks.INSCRIBED_JUNGLE.get(), M19Blocks.IMBUED_JUNGLE.get(), 
				M19Blocks.INSCRIBED_MANGROVE.get(), M19Blocks.IMBUED_MANGROVE.get(), 
				M19Blocks.INSCRIBED_OAK.get(), M19Blocks.IMBUED_OAK.get(), 
				M19Blocks.INSCRIBED_SPRUCE.get(), M19Blocks.IMBUED_SPRUCE.get(), 
				M19Blocks.INSCRIBED_WARPED.get(), M19Blocks.IMBUED_WARPED.get());
		tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
				M19Blocks.INSCRIBED_STONE.get(), M19Blocks.IMBUED_STONE.get(),
				M19Blocks.CRUCIBLE.get(),
				M19Blocks.FAIRY_JAR.get());
	}
}