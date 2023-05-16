package com.lying.misc19.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.lying.misc19.client.renderer.ComponentRenderers;
import com.lying.misc19.client.renderer.RenderUtils;
import com.lying.misc19.client.renderer.magic.ComponentRenderer;
import com.lying.misc19.client.renderer.magic.PixelProvider;
import com.lying.misc19.init.SpellComponents;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.reference.Reference;
import com.lying.misc19.utility.M19Utils;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

public class SpellTexture 
{
	private static final Minecraft mc = Minecraft.getInstance();
	private static final int TEX_SIZE = 1000;
	public static final ResourceLocation TEXTURE_LOCATION_1 = new ResourceLocation(Reference.ModInfo.MOD_ID, "magic/editor_display");
	public static final ResourceLocation TEXTURE_LOCATION_2 = new ResourceLocation(Reference.ModInfo.MOD_ID, "magic/editor_held");
	
	public static SpellTexture EDITOR = new SpellTexture(TEXTURE_LOCATION_1);
	public static SpellTexture HELD = new SpellTexture(TEXTURE_LOCATION_2);
	
	final ResourceLocation textureLocation;
	NativeImage image = new NativeImage(TEX_SIZE, TEX_SIZE, false);
	DynamicTexture tex = new DynamicTexture(image);
	final int resolution = 1;	// How many pixels in the texture vs rendered image, usually a power of 2
	
	private Vec2 centre = new Vec2(TEX_SIZE / 2, TEX_SIZE / 2);
	private int minX = TEX_SIZE / 2, minY = TEX_SIZE / 2;
	private int maxX = 0, maxY = 0;
	
	private Map<Integer, List<PixelProvider>> layerMap = new HashMap<>();
	
	boolean dirty = true;
	
	public SpellTexture()
	{
		this(TEXTURE_LOCATION_1);
	}
	
	public SpellTexture(ResourceLocation location)
	{
		this.textureLocation = location;
		mc.textureManager.register(location, this.tex);
	}
	
	public int width() { return image.getWidth(); }
	public int height() { return image.getHeight(); }
	public int area() { return width() * height(); }
	
	public double usedArea() { return (maxX - minX) * (maxY - minY); }
	
	public void clear()
	{
		if(!this.dirty)
		{
			NativeImage blank = new NativeImage(image.getWidth(), image.getHeight(), false);
			image.copyFrom(blank);
			blank.close();
		}
		this.dirty = true;
	}
	
	public void close()
	{
		NativeImage blank = new NativeImage(image.getWidth(), image.getHeight(), false);
		image.copyFrom(blank);
		blank.close();
		this.tex.upload();
		
		image.close();
		tex.close();
	}
	
	public void update(@Nullable ISpellComponent arrangement)
	{
		clear();
		if(arrangement == null)
			return;
		
		ISpellComponent comp = SpellComponents.readFromNBT(ISpellComponent.saveToNBT(arrangement));
		comp.setPosition(TEX_SIZE / 2, TEX_SIZE / 2);
		
		layerMap.clear();
		addToTexture(comp, this::addPixels);
		
		for(Entry<Integer, List<PixelProvider>> entry : layerMap.entrySet())
		{
			int layer = entry.getKey();
			List<PixelProvider> conflictors = Lists.newArrayList();
			for(Entry<Integer, List<PixelProvider>> entry2 : layerMap.entrySet())
				if(entry2.getKey() < layer)
					conflictors.addAll(entry2.getValue());
			
			entry.getValue().forEach((provider) -> provider.applyTo(this, conflictors));
		}
	}
	
	private void addPixels(PixelProvider provider, int layer)
	{
		List<PixelProvider> set = layerMap.getOrDefault(layer, Lists.newArrayList());
		set.add(provider);
		layerMap.put(layer, set);
	}
	
	public static PixelProvider addCircle(int xIn, int yIn, int radius, float thickness, boolean isConflictor)
	{
		return new PixelProvider()
				{
					public void applyTo(SpellTexture texture, List<PixelProvider> conflictors)
					{
						texture.drawCircle(xIn, yIn, radius, conflictors);
					}
					public boolean shouldExclude(int x, int y) { return isConflictor ? new Vec2(x, y).distanceToSqr(new Vec2(xIn, yIn)) < (radius * radius) : false; }
				};
	}
	
	public static PixelProvider addLine(Vec2 a, Vec2 b)
	{
		return new PixelProvider()
				{
					public void applyTo(SpellTexture texture, List<PixelProvider> conflictors) { texture.drawLine(a, b, conflictors); }
				};
	}
	
	public static PixelProvider addPolygon(Vec2... points)
	{
		return new PixelProvider()
				{
					public void applyTo(SpellTexture texture, List<PixelProvider> conflictors)
					{
						for(int i=0; i<points.length; i++)
						{
							texture.drawLine(points[i], points[(i+1)%points.length], conflictors);
						}
					}
				};
	}
	
	private void addToTexture(ISpellComponent comp, BiConsumer<PixelProvider,Integer> func)
	{
		ComponentRenderer renderer = ComponentRenderers.get(comp.getRegistryName());
		renderer.addToTextureRecursive(comp, func);
	}
	
	public void render(int screenX, int screenY)
	{
		if(dirty)
		{
			this.tex.upload();
			dirty = false;
		}
		
		float wide = ((float)width() / resolution) / 2;
		float high = ((float)height() / resolution) / 2;
		Vec2[] vertices = new Vec2[]{
				new Vec2(screenX - wide, screenY - high),
				new Vec2(screenX + wide, screenY - high),
				new Vec2(screenX + wide, screenY + high),
				new Vec2(screenX - wide, screenY + high)};
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, this.textureLocation);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX, (buffer) -> 
		{
			buffer.vertex(vertices[0].x, vertices[0].y, 0).uv(0F, 0F).endVertex();
			buffer.vertex(vertices[3].x, vertices[3].y, 0).uv(0F, 1F).endVertex();
			buffer.vertex(vertices[2].x, vertices[2].y, 0).uv(1F, 1F).endVertex();
			buffer.vertex(vertices[1].x, vertices[1].y, 0).uv(1F, 0F).endVertex();
		});
	}
	
	public void drawCircle(int x, int y, int radius, List<PixelProvider> conflictors)
	{
		Vec2 origin = centre.add(new Vec2(x, y).add(centre.negated()).scale(resolution));
		Vec2 offset = new Vec2(0, radius).scale(resolution);
		double rads = Math.toRadians(11.25D);
		double cos = Math.cos(rads);
		double sin = Math.sin(rads);
		
		for(int i=0; i<32; i++)
		{
			Vec2 pos = origin.add(offset);
			Vec2 posB = origin.add(offset = M19Utils.rotate(offset, cos, sin));
			drawLineBetween(pos, posB, conflictors);
		}
	}
	
	public void drawLine(Vec2 a, Vec2 b, List<PixelProvider> conflictors)
	{
		drawLineBetween(a.scale(resolution), b.scale(resolution), conflictors);
	}
	
	private void drawLineBetween(Vec2 a, Vec2 b, List<PixelProvider> conflictors)
	{
		Vec2 dir = b.add(a.negated());
		double len = dir.length();
		dir = dir.normalized();
		
		for(float i=0; i<len; i+=0.5F)
		{
			Vec2 pos = a.add(dir.scale(i));
			setPixel((int)pos.x, (int)pos.y, conflictors);
		}
	}
	
	private void drawSquare(int minX, int minY, int maxX, int maxY, List<PixelProvider> conflictors)
	{
		Vec2 xy = new Vec2(minX, minY);
		Vec2 Xy = new Vec2(maxX, minY);
		Vec2 XY = new Vec2(maxX, maxY);
		Vec2 xY = new Vec2(minX, maxY);
		
		drawLine(xy, Xy, conflictors);
		drawLine(Xy, XY, conflictors);
		drawLine(XY, xY, conflictors);
		drawLine(xY, xy, conflictors);
	}
	
	private static final Vec2[] scanDirections = new Vec2[] 
			{
				new Vec2(+1, 0),
				new Vec2(-1, 0),
				new Vec2(0, +1),
				new Vec2(0, -1)
			};
	
	// FIXME Correct inside-polygon confirmation for shape fill
	/** Performs a simple flood fill on all points inside the given polygon */
	private void fillPolygon(List<PixelProvider> conflictors, Vec2... points)
	{
		if(points.length == 2)
		{
			drawLine(points[0], points[1], conflictors);
			return;
		}
		else if(points.length < 2)
			return;
		
		System.out.println("Starting to fill "+points.length+"-pointed polygon");
		List<Vec2> filled = Lists.newArrayList();
		List<Vec2> toBeFilled = Lists.newArrayList();
		Vec2 firstPoint = getFirstPointInside(points);
		if(firstPoint == null)
			return;
		
		toBeFilled.add(firstPoint);
		while(!toBeFilled.isEmpty())
		{
			System.out.println(" # "+toBeFilled.size()+" points to fill");
			List<Vec2> nextSet = Lists.newArrayList();
			
			for(Vec2 point : toBeFilled)
			{
				setPixel((int)point.x, (int)point.y, conflictors);
				filled.add(point);
				
				for(Vec2 dir : scanDirections)
				{
					Vec2 offset = point.add(dir);
					if(nextSet.contains(offset) || filled.contains(offset))
						continue;
					
					if(isInsidePolygon(offset, points))
						nextSet.add(offset);
				}
			}
			
			nextSet.removeAll(toBeFilled);
			toBeFilled.clear();
			toBeFilled.addAll(nextSet);
		}
		System.out.println(" # Complete, "+filled.size()+" points filled");
	}
	
	private static Vec2 getFirstPointInside(Vec2... points)
	{
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float maxY = Float.MIN_VALUE;
		for(Vec2 point : points)
		{
			if(point.x < minX)
				minX = point.x;
			if(point.x > maxX)
				maxX = point.x;
			if(point.y < minY)
				minY = point.y;
			if(point.y > maxY)
				maxY = point.y;
		}
		
		for(float x=minX; x<maxX; x++)
			for(float y=minY; y<maxY; y++)
				if(isInsidePolygon(new Vec2(x, y), points))
					return new Vec2(x, y);
		
		return null;
	}
	
	private static boolean isInsidePolygon(Vec2 point, Vec2... points)
	{
		if(points.length < 3)
			return false;
		
		double totalAngle = 0D;
		for(int i=0; i<points.length; i++)
		{
			Vec2 a = points[i];
			Vec2 b = points[(i+1)%points.length];
			totalAngle += angle(a.x - point.x, a.y - point.y, b.x - point.x, b.y - point.y);
		}
		return Math.abs(totalAngle) >= Math.PI;
	}
	
	private static double angle(double x1, double y1, double x2, double y2)
	{
		double theta1 = Math.atan2(y1,x1);
		double theta2 = Math.atan2(y2,x2);
		
		double dtheta = theta2 - theta1;
		while (dtheta > Math.PI)
			dtheta -= Math.PI * 2;
		while (dtheta < -Math.PI)
			dtheta += Math.PI * 2;
		
		return dtheta;
	}
	
	public boolean setPixel(int x, int y, List<PixelProvider> conflictors)
	{
		if(x < 0 || x >= width() || y < 0 || y >= height())
			return false;
		
		for(PixelProvider conflictor : conflictors)
			if(conflictor.shouldExclude(x, y))
				return false;
		
		image.setPixelRGBA(x, y, -1);
		
		if(x < minX)
			minX = x;
		if(x > maxX)
			maxX = x;
		
		if(y < minY)
			minY = y;
		if(y > maxY)
			maxY = y;
		
		this.dirty = true;
		return true;
	}
}
