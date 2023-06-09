package com.lying.circles.utility;

import com.lying.circles.data.CMBlockTags;
import com.lying.circles.data.CMItemTags;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

public class CMUtils
{
	/** Returns true if the player is holding or wearing any item or block that has the MAGICAL tag */
	public static boolean canSeeMagic(Player player)
	{
		for(EquipmentSlot slot : EquipmentSlot.values())
		{
			ItemStack stack = player.getItemBySlot(slot);
			if(stack.is(CMItemTags.MAGICAL) || stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock().defaultBlockState().is(CMBlockTags.MAGICAL))
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
	
	/** Returns true if the given point is inside the given polygon.<br>Does not perform a boundary check. */
	public static boolean isInsidePolygonIgnoreBounds(Vec2 p, Vec2... polygon)
	{
	    boolean inside = false;
	    for(int i = 0, j = polygon.length - 1; i < polygon.length; j = i++)
	        if((polygon[i].y > p.y) != (polygon[j].y > p.y) && p.x < (polygon[j].x - polygon[i].x) * (p.y - polygon[i].y) / (polygon[j].y - polygon[i].y) + polygon[i].x)
	            inside = !inside;
	    
	    return inside;
	}
	
	/** Returns true if the given point is inside the given polygon.<br>Performs a boundary check. */
	public static boolean isInsidePolygon(Vec2 p, Vec2... polygon)
	{
	    double minX = Double.MAX_VALUE;
	    double maxX = Double.MIN_VALUE;
	    double minY = Double.MAX_VALUE;
	    double maxY = Double.MIN_VALUE;
	    for (int i = 0; i < polygon.length; i++)
	    {
	    	Vec2 q = polygon[i];
	        minX = Math.min(q.x, minX);
	        maxX = Math.max(q.x, maxX);
	        minY = Math.min(q.y, minY);
	        maxY = Math.max(q.y, maxY);
	    }
	    
	    if(p.x < minX || p.x > maxX || p.y < minY || p.y > maxY)
	        return false;
	    
	    return isInsidePolygonIgnoreBounds(p, polygon);
	}
}
