package com.lying.misc19.client.renderer.magic.components;

import java.util.List;

import com.lying.misc19.client.SpellTexture;
import com.lying.misc19.utility.M19Utils;

import net.minecraft.world.phys.Vec2;

public class PixelPolygon implements PixelProvider
{
	private final boolean isConflictor;
	
	private final int points;
	private final float thickness;
	private final Vec2[] pointSet;
	
	public PixelPolygon(float thickness, Vec2... pointSetIn)
	{
		this(thickness, false, pointSetIn);
	}
	
	public PixelPolygon(float thickness, boolean isConflictor, Vec2... pointSetIn)
	{
		this.isConflictor = isConflictor;
		this.thickness = thickness;
		this.points = pointSetIn.length;
		this.pointSet = pointSetIn;
	}
	
	public static PixelPolygon regularPolygon(int points, float radius, Vec2 core, Vec2 up, float thickness)
	{
		return regularPolygon(points, radius, core, up, thickness, false);
	}
	
	public static PixelPolygon regularPolygon(int points, float radius, Vec2 core, Vec2 up, float thickness, boolean isConflictor)
	{
		Vec2[] pointSet = new Vec2[points];
		Vec2 offset = up.normalized().scale(radius);
		float turn = 360F / points;
		double rads = Math.toRadians(turn);
		double cos = Math.cos(rads);
		double sin = Math.sin(rads);
		for(int i=0; i<points; i++)
			pointSet[i] = core.add(offset = M19Utils.rotate(offset, cos, sin));
		
		return new PixelPolygon(thickness, isConflictor, pointSet);
	}
	
	public static PixelPolygon diamond(Vec2 core, Vec2 up, float radiusX, float radiusY, float thickness)
	{
		return diamond(core, up, radiusX, radiusY, thickness, false);
	}
	
	public static PixelPolygon diamond(Vec2 core, Vec2 up, float radius, float thickness)
	{
		return diamond(core, up, radius, thickness, false);
	}
	
	public static PixelPolygon diamond(Vec2 core, Vec2 up, float radius, float thickness, boolean isConflictor)
	{
		return diamond(core, up, radius, radius, thickness, isConflictor);
	}
	
	public static PixelPolygon diamond(Vec2 core, Vec2 up, float radiusX, float radiusY, float thickness, boolean isConflictor)
	{
		Vec2 right = M19Utils.rotate(up, 90D);
		
		Vec2 top = core.add(up.scale(radiusY));
		Vec2 rig = core.add(right.scale(radiusX)); 
		Vec2 bot = core.add(up.scale(-radiusY));
		Vec2 lef = core.add(right.scale(-radiusX));
		
		return new PixelPolygon(thickness, isConflictor, top, rig, bot, lef);
	}
	
	public void applyTo(SpellTexture texture, List<PixelProvider> conflictors)
	{
		if(thickness > 0F)
			texture.drawPolygon(conflictors, thickness, pointSet);
	}
	
	public boolean shouldExclude(int x, int y, int width, int height, int resolution)
	{
		if(!isConflictor)
			return false;
		
		// Centre of image
		Vec2 centre = new Vec2(width / 2, height / 2);
		
		// Adjust all points in set by resolution
		Vec2[] scaledPointSet = new Vec2[points];
		for(int i=0; i<points; i++)
			scaledPointSet[i] = centre.add(pointSet[i].add(centre.negated()).scale(resolution));
		
		return M19Utils.isInsidePolygon(new Vec2(x, y), scaledPointSet);
	}

}
