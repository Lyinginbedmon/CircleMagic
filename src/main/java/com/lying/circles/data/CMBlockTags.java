package com.lying.circles.data;

import javax.annotation.Nullable;

import com.lying.circles.init.CMBlocks;
import com.lying.circles.reference.Reference;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

public class CMBlockTags extends BlockTagsProvider
{
    public static final TagKey<Block> MAGICAL = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(Reference.ModInfo.MOD_ID, "magical"));
    public static final TagKey<Block> CURRUID = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(Reference.ModInfo.MOD_ID, "curruid"));
	
	public CMBlockTags(DataGenerator p_126511_, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(p_126511_, Reference.ModInfo.MOD_ID, existingFileHelper);
	}
	
	public String getName() { return "Circle Magic block tags"; }
	
	protected void addTags()
	{
		tag(MAGICAL).add(
				CMBlocks.CRUCIBLE.get(),
				CMBlocks.INSCRIBED_ACACIA.get(),
				CMBlocks.INSCRIBED_BIRCH.get(),
				CMBlocks.INSCRIBED_CRIMSON.get(),
				CMBlocks.INSCRIBED_DARK_OAK.get(),
				CMBlocks.INSCRIBED_JUNGLE.get(),
				CMBlocks.INSCRIBED_MANGROVE.get(),
				CMBlocks.INSCRIBED_OAK.get(),
				CMBlocks.INSCRIBED_SPRUCE.get(),
				CMBlocks.INSCRIBED_WARPED.get(),
				CMBlocks.INSCRIBED_STONE.get());
		
		tag(CURRUID).add(CMBlocks.CURRUID_BLOCK.get(), CMBlocks.CURRUID_DUST.get());
		
		tag(BlockTags.MINEABLE_WITH_AXE).add(
				CMBlocks.INSCRIBED_ACACIA.get(), CMBlocks.IMBUED_ACACIA.get(), 
				CMBlocks.INSCRIBED_BIRCH.get(), CMBlocks.IMBUED_BIRCH.get(), 
				CMBlocks.INSCRIBED_CRIMSON.get(), CMBlocks.IMBUED_CRIMSON.get(), 
				CMBlocks.INSCRIBED_DARK_OAK.get(), CMBlocks.IMBUED_DARK_OAK.get(), 
				CMBlocks.INSCRIBED_JUNGLE.get(), CMBlocks.IMBUED_JUNGLE.get(), 
				CMBlocks.INSCRIBED_MANGROVE.get(), CMBlocks.IMBUED_MANGROVE.get(), 
				CMBlocks.INSCRIBED_OAK.get(), CMBlocks.IMBUED_OAK.get(), 
				CMBlocks.INSCRIBED_SPRUCE.get(), CMBlocks.IMBUED_SPRUCE.get(), 
				CMBlocks.INSCRIBED_WARPED.get(), CMBlocks.IMBUED_WARPED.get());
		tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
				CMBlocks.INSCRIBED_STONE.get(), CMBlocks.IMBUED_STONE.get(),
				CMBlocks.CRUCIBLE.get(),
				CMBlocks.FAIRY_JAR.get());
	}
}