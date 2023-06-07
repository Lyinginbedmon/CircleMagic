package com.lying.misc19.item;

import com.lying.misc19.blocks.entity.FairyJarBlockEntity;
import com.lying.misc19.blocks.entity.FairyPersonalityModel;
import com.lying.misc19.init.M19BlockEntities;
import com.lying.misc19.init.M19Blocks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class FairyJarItem extends BlockItem
{
	public FairyJarItem(Properties p_40566_)
	{
		super(M19Blocks.FAIRY_JAR.get(), p_40566_);
	}
	
	public Rarity getRarity(ItemStack stack) { return Rarity.RARE; }
	
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected)
	{
		CompoundTag blockData = BlockItem.getBlockEntityData(stack);
		if(world == null || world.isClientSide() || stack.getCount() != 1 || (blockData != null && !blockData.isEmpty()))
			return;
		
		FairyPersonalityModel personality = FairyJarBlockEntity.makeRandomPersonality(world.random);
		CompoundTag data = personality.saveToBlockTag(new CompoundTag());
		
		if(blockData == null)
			blockData = new CompoundTag();
		blockData.put("BlockEntityTag", data);
		BlockItem.setBlockEntityData(stack, M19BlockEntities.FAIRY_JAR.get(), data);
	}
}
