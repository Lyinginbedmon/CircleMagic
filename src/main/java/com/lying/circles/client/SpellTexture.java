package com.lying.circles.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.client.renderer.RenderUtils;
import com.lying.circles.client.renderer.magic.ComponentRenderers;
import com.lying.circles.client.renderer.magic.components.ComponentRenderer;
import com.lying.circles.client.renderer.magic.components.PixelPolygon;
import com.lying.circles.client.renderer.magic.components.PixelProvider;
import com.lying.circles.init.SpellComponents;
import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.utility.CMUtils;
import com.lying.circles.utility.SpellTextureManager;
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
	
	public static PixelProvider addCircle(Vec2 core, int radius, float thickness, boolean isConflictor)
	{
		return new PixelProvider()
				{
					public void applyTo(SpellTexture texture, List<PixelProvider> conflictors)
					{
						texture.drawCircle(core, radius, thickness, conflictors);
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
						Vec2 coreScaled = centre.add(core.add(centre.negated()).scale(resolution));
						
						// Radius of the circle modified by resolution
						int size = radius * resolution;
						return point.distanceToSqr(coreScaled) < (size * size);
					}
				};
	}
	
	public static PixelProvider addLine(Vec2 a, Vec2 b, float thickness)
	{
		return new PixelProvider()
				{
					public void applyTo(SpellTexture texture, List<PixelProvider> conflictors) { texture.drawLine(a, b, thickness, conflictors); }
				};
	}
	
	public static PixelProvider addPolygon(float thickness, Vec2... points)
	{
		return new PixelPolygon(thickness, points);
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
			buffer.vertex(matrix, vertices[0].x, vertices[0].y, 0F).color(255, 255, 255, 255).uv(texXMin, texYMin).uv2(255).endVertex();
			buffer.vertex(matrix, vertices[1].x, vertices[1].y, 0F).color(255, 255, 255, 255).uv(texXMax, texYMin).uv2(255).endVertex();
			buffer.vertex(matrix, vertices[2].x, vertices[2].y, 0F).color(255, 255, 255, 255).uv(texXMax, texYMax).uv2(255).endVertex();
			buffer.vertex(matrix, vertices[3].x, vertices[3].y, 0F).color(255, 255, 255, 255).uv(texXMin, texYMax).uv2(255).endVertex();
		matrixStack.popPose();
		
		matrixStack.pushPose();
			buffer.vertex(matrix, vertices[3].x, vertices[3].y, 0F).color(255, 255, 255, 255).uv(texXMin, texYMax).uv2(255).endVertex();
			buffer.vertex(matrix, vertices[2].x, vertices[2].y, 0F).color(255, 255, 255, 255).uv(texXMax, texYMax).uv2(255).endVertex();
			buffer.vertex(matrix, vertices[1].x, vertices[1].y, 0F).color(255, 255, 255, 255).uv(texXMax, texYMin).uv2(255).endVertex();
			buffer.vertex(matrix, vertices[0].x, vertices[0].y, 0F).color(255, 255,255, 255).uv(texXMin, texYMin).uv2(255).endVertex();
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
	
	public void drawCircle(Vec2 core, int radius, float thickness, List<PixelProvider> conflictors)
	{
		if(thickness <= 0F)
			return;
		
		core = centre.add(core.add(centre.negated()).scale(resolution));
		radius *= resolution;
		thickness *= resolution;
		
		double outerRadius = radius + (thickness / 2);
		double innerRadius = radius - (thickness / 2);
		int diameter = (int)(outerRadius * 2);
		float minX = (float) (core.x - outerRadius);
		float minY = (float) (core.y - outerRadius);
		for(int i=0; i<diameter; i++)
			for(int j=0; j<diameter; j++)
			{
				Vec2 point = new Vec2(i + minX, j + minY);
				double dist = Math.sqrt(point.distanceToSqr(core));
				if(dist <= outerRadius && dist >= innerRadius)
					setPixel((int)point.x, (int)point.y, conflictors);
			}
	}
	
	public void drawLine(Vec2 a, Vec2 b, float thickness, List<PixelProvider> conflictors)
	{
		a = this.centre.add(a.add(this.centre.negated()).scale(resolution));
		b = this.centre.add(b.add(this.centre.negated()).scale(resolution));
		
		Vec2 dir = CMUtils.rotate(b.add(a.negated()).normalized(), 90F).scale((thickness * this.resolution) / 2);
		Vec2 topLeft = a.add(dir);
		Vec2 topRight = a.add(dir.negated());
		Vec2 botRight = b.add(dir.negated());
		Vec2 botLeft = b.add(dir);
		
		fillPolygon(conflictors, topLeft, topRight, botRight, botLeft);
	}
	
	public void drawPolygon(List<PixelProvider> conflictors, float thickness, Vec2... polygon)
	{
		// Adjust incoming points for resolution
		thickness *= resolution;
		for(int i=0; i<polygon.length; i++)
			polygon[i] = centre.add(polygon[i].add(centre.negated()).scale(resolution));
		
		int prevIndex = polygon.length - 1;
		for(int i=0; i<polygon.length; i++)
		{
			Vec2 a = polygon[i];
			Vec2 prev = polygon[prevIndex % polygon.length];
			Vec2 next = polygon[(i + 1) % polygon.length];
			
			Vec2 toNext = next.add(a.negated()).normalized();
			Vec2 toPrev = prev.add(a.negated()).normalized();
			
			Vec2 aNorm = toNext.add(toPrev).scale(0.5F * thickness);
			Vec2 topRight = a.add(aNorm);
			Vec2 topLeft = a.add(aNorm.negated());
			
			Vec2 b = next;
			prev = a;
			next = polygon[(i + 2) % polygon.length];
			toNext = next.add(b.negated()).normalized();
			toPrev = prev.add(b.negated()).normalized();
			
			Vec2 bNorm = toNext.add(toPrev).normalized().scale(0.5F * thickness);
			Vec2 botRight = b.add(bNorm);
			Vec2 botLeft = b.add(bNorm.negated());
			
			fillPolygon(conflictors, topLeft, topRight, botRight, botLeft);
			prevIndex = ++prevIndex % polygon.length;
		}
	}
	
	private void fillPolygon(List<PixelProvider> conflictors, Vec2... polygon)
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
		
		for(double x = minX; x < maxX; x++)
			for(double y = minY; y < maxY; y++)
				if(CMUtils.isInsidePolygonIgnoreBounds(new Vec2((float)x, (float)y), polygon))
					setPixel((int)x, (int)y, conflictors);
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
