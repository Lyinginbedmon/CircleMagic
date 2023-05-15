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
			image.copyFrom(new NativeImage(TEX_SIZE, TEX_SIZE, false));
		this.dirty = true;
	}
	
	public void close()
	{
		image.copyFrom(new NativeImage(TEX_SIZE, TEX_SIZE, false));
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
	
	public static PixelProvider addCircle(int xIn, int yIn, int radius)
	{
		return new PixelProvider()
				{
					public void applyTo(SpellTexture texture, List<PixelProvider> conflictors) { texture.drawCircle(xIn, yIn, radius, conflictors); }
				};
	}
	
	public static PixelProvider addCircleConflictor(int xIn, int yIn, int radius)
	{
		return new PixelProvider()
				{
					public void applyTo(SpellTexture texture, List<PixelProvider> conflictors) { texture.drawCircle(xIn, yIn, radius, conflictors); }
					public boolean shouldExclude(int x, int y) { return new Vec2(x, y).distanceToSqr(new Vec2(xIn, yIn)) < (radius * radius); }
				};
	}
	
	public static PixelProvider addLine(Vec2 a, Vec2 b)
	{
		return new PixelProvider()
				{
					public void applyTo(SpellTexture texture, List<PixelProvider> conflictors) { texture.drawLine(a, b, conflictors); }
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
	
	public void drawLineBetween(Vec2 a, Vec2 b, List<PixelProvider> conflictors)
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
	
	public void drawSquare(int minX, int minY, int maxX, int maxY, List<PixelProvider> conflictors)
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
	
	public void setPixel(int x, int y, List<PixelProvider> conflictors)
	{
		if(x < 0 || x >= width() || y < 0 || y >= height())
			return;
		
		for(PixelProvider conflictor : conflictors)
			if(conflictor.shouldExclude(x, y))
				return;
		
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
	}
}
