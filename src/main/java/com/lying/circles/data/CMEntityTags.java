package com.lying.circles.data;

import com.lying.circles.reference.Reference;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;

public class CMEntityTags extends EntityTypeTagsProvider
{
	public static final TagKey<EntityType<?>> HAS_MANA = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(Reference.ModInfo.MOD_ID, "has_mana"));
	
	public CMEntityTags(DataGenerator p_126517_, ExistingFileHelper helperIn)
	{
		super(p_126517_, Reference.ModInfo.MOD_ID, helperIn);
	}
	
	public String getName() { return "Circle Magic entity tags"; }
	
	protected void addTags()
	{
		tag(HAS_MANA)
			.add(EntityType.PIGLIN, EntityType.PIGLIN_BRUTE)
			.add(EntityType.PILLAGER, EntityType.VINDICATOR)
			.add(EntityType.EVOKER, EntityType.ILLUSIONER)
			.add(EntityType.VILLAGER)
			.add(EntityType.PLAYER)
			.add(EntityType.WITCH);
	}
}
