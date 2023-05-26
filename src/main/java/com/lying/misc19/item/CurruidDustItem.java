package com.lying.misc19.item;

import com.lying.misc19.init.M19Blocks;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;

public class CurruidDustItem extends BlockItem
{
	public CurruidDustItem(Properties propertiesIn)
	{
		super(M19Blocks.CURRUID_DUST.get(), propertiesIn.craftRemainder(Items.GLASS_BOTTLE));
	}
	
	public InteractionResult place(BlockPlaceContext context)
	{
		InteractionResult result = super.place(context);
		if(result == InteractionResult.CONSUME)
			context.getPlayer().addItem(context.getItemInHand().getCraftingRemainingItem());
		return result;
	}
}
