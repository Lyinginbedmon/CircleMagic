package com.lying.misc19.utility;

import net.minecraft.world.phys.Vec2;

public class M19Utils
{
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
