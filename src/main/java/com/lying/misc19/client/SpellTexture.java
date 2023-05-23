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
import com.lying.misc19.utility.SpellTextureManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

public class SpellTexture 
{
	private static final Minecraft mc = Minecraft.getInstance();
	private final int resolution;	// How many pixels in the texture vs rendered image, usually a power of 2
	private final int tex_size;
	
	private final ResourceLocation textureLocation;
	private NativeImage image;
	private DynamicTexture tex;
	
	private final Vec2 centre;
	private int minX, minY;
	private int maxX = 0, maxY = 0;
	
	private Map<Integer, List<PixelProvider>> layerMap = new HashMap<>();
	
	boolean dirty = true;
	
	public SpellTexture(int resolutionIn)
	{
		this(SpellTextureManager.TEXTURE_EDITOR_MAIN, resolutionIn);
	}
	
	public SpellTexture(ResourceLocation location, int resolutionIn)
	{
		this.textureLocation = location;
		this.resolution = resolutionIn;
		this.tex_size = resolution * 1000;
		
		image = new NativeImage(tex_size, tex_size, false);
		tex = new DynamicTexture(image);
		mc.textureManager.register(location, this.tex);
		
		centre = new Vec2(tex_size / 2, tex_size / 2);
		minX = tex_size / 2;
		minY = tex_size / 2;
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
		comp.setPosition(tex_size / 2, tex_size / 2);
		
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
						texture.drawCircle(xIn, yIn, radius, thickness, conflictors);
					}
					
					public boolean shouldExclude(int x, int y, int width, int height, int resolution)
					{
						if(!isConflictor)
							return false;
						
						// Real position of the given pixel
						Vec2 point = new Vec2(x, y);
						
						// Centre of image
						Vec2 centre = new Vec2(width / 2, height / 2);
						
						// Centre of the circle modified by resolution
						Vec2 core = new Vec2(centre.x + (xIn - centre.x) * resolution, centre.y + (yIn - centre.y) * resolution);
						
						// Radius of the circle modified by resolution
						int size = radius * resolution;
						return point.distanceToSqr(core) < (size * size);
					}
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
		checkDirty();
		float wide = ((float)width() / resolution) * 0.5F;
		float high = ((float)height() / resolution) * 0.5F;
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
	
	public void render(PoseStack matrixStack, MultiBufferSource bufferSource)
	{
		checkDirty();
		Vec2 fullMax = new Vec2(width(), height());
		Vec2 centre = fullMax.scale(0.5F);
		
		Vec2 realMin = new Vec2(minX, minY);
		Vec2 realMax = new Vec2(maxX, maxY);
		
		float texXMin = realMin.x / width();
		float texXMax = realMax.x / width();
		float texYMin = realMin.y / height();
		float texYMax = realMax.y / height();
		
		float xMin = realMin.add(centre.negated()).x / resolution;
		float yMin = realMin.add(centre.negated()).y / resolution;
		float xMax = realMax.add(centre.negated()).x / resolution;
		float yMax = realMax.add(centre.negated()).y / resolution;
		
		Vec2[] vertices = new Vec2[]{
				new Vec2(xMin, yMin),
				new Vec2(xMax, yMin),
				new Vec2(xMax, yMax),
				new Vec2(xMin, yMax)};
		
		VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(textureLocation));
		Matrix4f matrix = matrixStack.last().pose();
		
		matrixStack.pushPose();
			buffer.vertex(matrix, vertices[0].x, vertices[0].y, 0F).color(255, 255, 255, 255).uv(texXMax, texYMin).uv2(255).endVertex();
			buffer.vertex(matrix, vertices[1].x, vertices[1].y, 0F).color(255, 255, 255, 255).uv(texXMin, texYMin).uv2(255).endVertex();
			buffer.vertex(matrix, vertices[2].x, vertices[2].y, 0F).color(255, 255, 255, 255).uv(texXMin, texYMax).uv2(255).endVertex();
			buffer.vertex(matrix, vertices[3].x, vertices[3].y, 0F).color(255, 255, 255, 255).uv(texXMax, texYMax).uv2(255).endVertex();
		matrixStack.popPose();
		
		matrixStack.pushPose();
			buffer.vertex(matrix, vertices[3].x, vertices[3].y, 0F).color(255, 255, 255, 255).uv(texXMin, texYMax).uv2(255).endVertex();
			buffer.vertex(matrix, vertices[2].x, vertices[2].y, 0F).color(255, 255, 255, 255).uv(texXMax, texYMax).uv2(255).endVertex();
			buffer.vertex(matrix, vertices[1].x, vertices[1].y, 0F).color(255, 255, 255, 255).uv(texXMax, texYMin).uv2(255).endVertex();
			buffer.vertex(matrix, vertices[0].x, vertices[0].y, 0F).color(255, 255, 255, 255).uv(texXMin, texYMin).uv2(255).endVertex();
		matrixStack.popPose();
	}
	
	protected void checkDirty()
	{
		if(dirty)
		{
			this.tex.upload();
			dirty = false;
		}
	}
	
	public void drawCircle(int x, int y, int radius, float thickness, List<PixelProvider> conflictors)
	{
		x = (int)(centre.x + (x - centre.x) * resolution);
		y = (int)(centre.y + (y - centre.y) * resolution);
		radius *= resolution;
		thickness *= resolution;
		
		double outerRadius = radius + (thickness / 2);
		double innerRadius = radius - (thickness / 2);
		int diameter = (int)(outerRadius * 2);
		float minX = (float) (x - outerRadius);
		float minY = (float) (y - outerRadius);
		for(int i=0; i<diameter; i++)
			for(int j=0; j<diameter; j++)
			{
				Vec2 point = new Vec2(i + minX, j + minY);
				double dist = Math.sqrt(point.distanceToSqr(new Vec2(x, y)));
				if(dist <= outerRadius && dist >= innerRadius)
					setPixel((int)point.x, (int)point.y, conflictors);
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
			if(conflictor.shouldExclude(x, y, width(), height(), resolution))
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
