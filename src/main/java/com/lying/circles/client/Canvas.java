package com.lying.circles.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.client.renderer.RenderUtils;
import com.lying.circles.client.renderer.magic.ComponentRenderers;
import com.lying.circles.client.renderer.magic.components.ComponentRenderer;
import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.utility.CMUtils;
import com.lying.circles.utility.SpellTextureManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

/** Cohesive layered drawing object */
public class Canvas
{
	public static final int SPRITES = 0;
	public static final int GLYPHS = 5;
	public static final int EXCLUSIONS = 10;
	public static final int DECORATIONS = 15;
	
	private final SpellTexture texture;
	private final Map<Integer, List<ICanvasObject>> elements = new HashMap<>();
	
	public Canvas()
	{
		this(new SpellTexture(SpellTextureManager.TEXTURE_EDITOR_HELD, 2));
	}
	
	public Canvas(ResourceLocation locationIn, int resolution)
	{
		this(new SpellTexture(locationIn, resolution));
	}
	
	public Canvas(SpellTexture textureIn)
	{
		texture = textureIn;
	}
	
	public void close()
	{
		this.clear();
		this.texture.close();
	}
	
	public void clear()
	{
		this.elements.clear();
		this.texture.clear();
	}
	
	public SpellTexture texture() { return this.texture; }
	
	public void populate(ISpellComponent component)
	{
		ResourceLocation registryName = component.getRegistryName();
		ComponentRenderer renderer = ComponentRenderers.get(registryName);
		
		renderer.addToCanvasRecursive(component, this);
		texture().update(component);
	}
	
	public void addElement(ICanvasObject object, int zLevel)
	{
		List<ICanvasObject> objects = elements.getOrDefault(Math.max(0, zLevel), Lists.newArrayList());
		objects.add(object);
		elements.put(zLevel, objects);
	}
	
	public void drawIntoGUI(PoseStack matrixStack, int posX, int posY, int width, int height)
	{
		this.texture.render(posX, posY);
		draw(matrixStack, (element, matrix, exclusions) -> element.drawGui(matrixStack, exclusions, width, height));
	}
	
	public void drawIntoWorld(PoseStack matrixStack, MultiBufferSource bufferSource)
	{
		matrixStack.pushPose();
			float scale = 0.005F;
			matrixStack.scale(scale, -scale, scale);
			this.texture.render(matrixStack, bufferSource);
			draw(matrixStack, (element, matrix, exclusions) -> element.drawWorld(matrixStack, bufferSource, exclusions));
		matrixStack.popPose();
	}
	
	private void draw(PoseStack matrixStack, TriConsumer<ICanvasObject, PoseStack, List<Quad>> func)
	{
		List<Integer> levels = Lists.newArrayList();
		levels.addAll(elements.keySet());
		levels.sort(Collections.reverseOrder());
		
		for(int i : levels)
			elements.get(i).forEach((element) -> { if(!element.isExclusion()) func.accept(element,matrixStack, getExclusionsBelow(i)); });
	}
	
	public List<Quad> getExclusionsBelow(int level)
	{
		List<Quad> exclusions = Lists.newArrayList();
		for(Entry<Integer, List<ICanvasObject>> entry : elements.entrySet())
			if(entry.getKey() < level)
				entry.getValue().forEach((object) -> {
					if(object.isExclusion())
						exclusions.addAll(((ICanvasExclusion)object).getQuads()); } );
		return exclusions;
	}
	
	public interface ICanvasObject
	{
		public void drawGui(PoseStack matrixStack, List<Quad> exclusions, int width, int height);
		
		public void drawWorld(PoseStack matrixStack, MultiBufferSource bufferSource, List<Quad> exclusions);
		
		public default boolean isExclusion() { return false; }
	}
	
	public interface ICanvasExclusion extends ICanvasObject
	{
		public default boolean isExclusion() { return true; }
		
		public List<Quad> getQuads();
		
		public default void drawGui(PoseStack matrixStack, List<Quad> exclusions, int width, int height) { }
		public default void drawWorld(PoseStack matrixStack, MultiBufferSource bufferSource, List<Quad> exclusions) { }
	}
	
	public static class Circle implements ICanvasObject
	{
		private final Vec2 position;
		private final float radius, thickness;
		private final int r, g, b, a;
		
		private final List<Quad> quads = Lists.newArrayList();
		
		public Circle(Vec2 pos, float radiusIn, float thicknessIn)
		{
			this(pos, radiusIn, thicknessIn, 255, 255, 255, 255);
		}
		
		public Circle(Vec2 pos, float radiusIn, float thicknessIn, int red, int green, int blue, int alpha)
		{
			this.position = pos;
			this.radius = radiusIn;
			this.thickness = thicknessIn;
			this.r = red;
			this.g = green;
			this.b = blue;
			this.a = alpha;
			
			int resolution = (int)((2 * Math.PI * radius) / RenderUtils.CIRCLE_UNIT);
			Vec2 offsetOut = new Vec2(radius + thickness / 2, 0);
			Vec2 offsetIn = new Vec2(radius - thickness / 2, 0);
			
			float turn = 360F / resolution;
			double rads = Math.toRadians(turn);
			double cos = Math.cos(rads), sin = Math.sin(rads);
			
			for(int i=0; i<resolution; i++)
				quads.add(new Quad(pos.add(offsetOut), pos.add(offsetIn), pos.add(offsetIn = CMUtils.rotate(offsetIn, cos, sin)), pos.add(offsetOut = CMUtils.rotate(offsetOut, cos, sin))));
		}
		
		public void drawGui(PoseStack matrixStack, List<Quad> exclusions, int width, int height)
		{
			for(Quad quad : quads)
				if(quad.isWithinScreen(width, height))
					RenderUtils.drawBlockColorSquare(quad, r, g, b, a, exclusions);
		}
		
		public void drawWorld(PoseStack matrixStack, MultiBufferSource bufferSource, List<Quad> exclusions)
		{
			for(Quad quad : quads)
				if(!quad.isUndrawable())
					;
			RenderUtils.drawOutlineCircle(matrixStack, bufferSource, position, radius, thickness, r, g, b, a);
		}
	}
	
	public static class Line implements ICanvasObject
	{
		private final Vec2 start, end;
		private final float thickness;
		private final int r, g, b, a;
		
		public Line(Vec2 posA, Vec2 posB, float thickness)
		{
			this(posA, posB, thickness, 255, 255, 255, 255);
		}
		
		public Line(Vec2 posA, Vec2 posB, float thickness, int red, int green, int blue, int alpha)
		{
			this.start = posA;
			this.end = posB;
			this.thickness = thickness;
			this.r = red;
			this.g = green;
			this.b = blue;
			this.a = alpha;
		}
		
		public void drawGui(PoseStack matrixStack, List<Quad> exclusions, int width, int height)
		{
			RenderUtils.drawColorLine(start, end, thickness, r, g, b, a, exclusions);
		}
		
		public void drawWorld(PoseStack matrixStack, MultiBufferSource bufferSource, List<Quad> exclusions)
		{
			RenderUtils.drawColorLine(matrixStack, bufferSource, start, end, thickness, r, g, b, a);
		}
	}
	
	public static class Sprite implements ICanvasObject
	{
		private final ResourceLocation textureLocation;
		private final int color;
		private final Vec2[] vertices;
		
		public Sprite(ResourceLocation texture, Vec2 position, int width, int height, int color)
		{
			this.textureLocation = texture;
			this.color = color;
			vertices = new Vec2[]{
					new Vec2(position.x - width / 2, position.y - height / 2),
					new Vec2(position.x + width / 2, position.y - height / 2),
					new Vec2(position.x + width / 2, position.y + height / 2),
					new Vec2(position.x - width / 2, position.y + height / 2)};
		}
		
		public void drawGui(PoseStack matrixStack, List<Quad> exclusions, int width, int height)
		{
			boolean outOfBounds = true;
			for(Vec2 vertex : vertices)
				if(vertex.x >= 0 && vertex.x <= width && vertex.y >= 0 && vertex.y <= height)
				{
					outOfBounds = false;
					break;
				}
			if(outOfBounds)
				return;
			
		    RenderSystem.setShader(GameRenderer::getPositionTexShader);
		    RenderSystem.setShaderTexture(0, textureLocation);
		    
		    float r = ((color & 0xff0000) >> 16) / 255F;
		    float g = ((color & 0x00ff00) >> 8) / 255F;;
		    float b = (color & 0x0000ff) / 255F;
		    RenderSystem.setShaderColor(r, g, b, 1F);
			RenderUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX, (buffer) -> 
			{
				buffer.vertex(vertices[0].x, vertices[0].y, 0).uv(0F, 0F).endVertex();
				buffer.vertex(vertices[3].x, vertices[3].y, 0).uv(0F, 1F).endVertex();
				buffer.vertex(vertices[2].x, vertices[2].y, 0).uv(1F, 1F).endVertex();
				buffer.vertex(vertices[1].x, vertices[1].y, 0).uv(1F, 0F).endVertex();
			});
		    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		}
		
		public void drawWorld(PoseStack matrixStack, MultiBufferSource bufferSource, List<Quad> exclusions)
		{
			Vec2 topLeft = vertices[0];
			Vec2 topRight = vertices[1];
			Vec2 botRight = vertices[2];
			Vec2 botLeft = vertices[3];
			VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(textureLocation));
			Matrix4f matrix = matrixStack.last().pose();
			
			matrixStack.pushPose();
				buffer.vertex(matrix, topLeft.x, topLeft.y, 0F).color(255, 255, 255, 255).uv(1F, 0F).uv2(255).endVertex();
				buffer.vertex(matrix, topRight.x, topRight.y, 0F).color(255, 255, 255, 255).uv(0F, 0F).uv2(255).endVertex();
				buffer.vertex(matrix, botRight.x, botRight.y, 0F).color(255, 255, 255, 255).uv(0F, 1F).uv2(255).endVertex();
				buffer.vertex(matrix, botLeft.x, botLeft.y, 0F).color(255, 255, 255, 255).uv(1F, 1F).uv2(255).endVertex();
			matrixStack.popPose();
			
			matrixStack.pushPose();
				buffer.vertex(matrix, botLeft.x, botLeft.y, 0F).color(255, 255, 255, 255).uv(0F, 1F).uv2(255).endVertex();
				buffer.vertex(matrix, botRight.x, botRight.y, 0F).color(255, 255, 255, 255).uv(1F, 1F).uv2(255).endVertex();
				buffer.vertex(matrix, topRight.x, topRight.y, 0F).color(255, 255, 255, 255).uv(1F, 0F).uv2(255).endVertex();
				buffer.vertex(matrix, topLeft.x, topLeft.y, 0F).color(255, 255, 255, 255).uv(0F, 0F).uv2(255).endVertex();
			matrixStack.popPose();
		}
	}
	
	/** Defines a quad where canvas objects below should not be drawn */
	public static class ExclusionQuad implements ICanvasExclusion
	{
		private final Quad quad;
		
		public ExclusionQuad(Vec2 xyIn, Vec2 XyIn, Vec2 XYin, Vec2 xYIn)
		{
			this.quad = new Quad(xyIn, XyIn, XYin, xYIn);
		}
		
		public List<Quad> getQuads(){ return List.of(this.quad); }
	}
	
	/** Defines a circle of quads where canvas objects below should not be drawn */
	public static class ExclusionCircle implements ICanvasExclusion
	{
		private static final float TURN = 360F / 20F;
		private static final double COS = Math.cos(TURN);
		private static final double SIN = Math.sin(TURN);
		private static final double COS2 = Math.cos(TURN / 2);
		private static final double SIN2 = Math.sin(TURN / 2);
		private final List<Quad> quads = Lists.newArrayList();
		
		public ExclusionCircle(Vec2 pos, float radius)
		{	
			Vec2 offset = new Vec2(radius, 0);
			for(int i=0; i<360 / TURN; i++)
				quads.add(new Quad(pos, pos.add(offset), pos.add(CMUtils.rotate(offset, COS2, SIN2)), pos.add(offset = CMUtils.rotate(offset, COS, SIN))));
		}
		
		public List<Quad> getQuads() { return this.quads; }
	}
}
