package com.lying.misc19.utility;

import com.lying.misc19.data.M19BlockTags;
import com.lying.misc19.data.M19ItemTags;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

public class M19Utils
{
	/** Returns true if the player is holding or wearing any item or block that has the MAGICAL tag */
	public static boolean canSeeMagic(Player player)
	{
		for(EquipmentSlot slot : EquipmentSlot.values())
		{
			ItemStack stack = player.getItemBySlot(slot);
			if(stack.is(M19ItemTags.MAGICAL) || stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock().defaultBlockState().is(M19BlockTags.MAGICAL))
				return true;
		}
		
		return false;
	}
	
	public static Vec2 rotate(Vec2 vec, double degrees)
	{
		double rads = Math.toRadians(degrees);
		return rotate(vec, Math.cos(rads), Math.sin(rads));
	}
	
	public static Vec2 rotate(Vec2 vec, double cos, double sin)
	{
		return new Vec2((float)(vec.x * cos - vec.y * sin), (float)(vec.y * cos + vec.x * sin));
	}
}
