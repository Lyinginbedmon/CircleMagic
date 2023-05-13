package com.lying.misc19.client.renderer;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.compress.utils.Lists;

import com.google.common.base.Function;
import com.lying.misc19.client.Quad;
import com.lying.misc19.client.Quad.Line;
import com.lying.misc19.utility.M19Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;

// FIXME Amend world draw functions to accept exclusions
public class RenderUtils
{
	public static final double CIRCLE_UNIT = 3D;
	
	/** Draws a coloured line into a GUI screen between the given points */
	public static void drawColorLine(Vec2 posA, Vec2 posB, float width, List<Quad> exclusions)
	{
		drawColorLine(posA, posB, width, 255, 255, 255, 255, exclusions);
	}
	
	/** Draws a coloured line into a GUI screen between the given points */
	public static void drawColorLine(Vec2 posA, Vec2 posB, float width, int r, int g, int b, int a, List<Quad> exclusions)
	{
		Vec2 offset = M19Utils.rotate(posB.add(posA.negated()).normalized(), 90D);
		Vec2 topRight = posA.add(offset.scale(width / 2));
		Vec2 topLeft = posA.add(offset.scale(width / 2).negated());
		Vec2 botLeft = posB.add(offset.scale(width / 2).negated());
		Vec2 botRight = posB.add(offset.scale(width / 2));
		
		drawBlockColorSquare(new Quad(topLeft, topRight, botRight, botLeft), r, g, b, a, exclusions);
	}
	
	/** Draws a coloured line into the world between the given points */
	public static void drawColorLine(PoseStack matrixStack, MultiBufferSource bufferSource, Vec2 posA, Vec2 posB, float width, int r, int g, int b, int a)
	{
		Vec2 offset = M19Utils.rotate(posB.add(posA.negated()).normalized(), 90D);
		Vec2 topRight = posA.add(offset.scale(width / 2));
		Vec2 topLeft = posA.add(offset.scale(width / 2).negated());
		Vec2 botLeft = posB.add(offset.scale(width / 2).negated());
		Vec2 botRight = posB.add(offset.scale(width / 2));
		
		drawBlockColorSquare(matrixStack, bufferSource, topLeft, topRight, botLeft, botRight, r, g, b, a);
	}
	
	public static void drawOutlineColorSquare(Vec2 centre, float width, float height, float thickness, int r, int g, int b, int a)
	{
		// TODO Revise to quad approach for clean pointed corners
//		float minX = centre.x - width / 2;
//		float maxX = centre.x + width / 2;
//		float minY = centre.y - height / 2;
//		float maxY = centre.y + height / 2;
		
//		RenderUtils.drawColorLine(new Vec2(minX, minY), new Vec2(minX, maxY), thickness, r, g, b, a);
//		RenderUtils.drawColorLine(new Vec2(maxX, minY), new Vec2(maxX, maxY), thickness, r, g, b, a);
//		RenderUtils.drawColorLine(new Vec2(minX, minY), new Vec2(maxX, minY), thickness, r, g, b, a);
//		RenderUtils.drawColorLine(new Vec2(minX, maxY), new Vec2(maxX, maxY), thickness, r, g, b, a);
	}
	
	public static void drawBlockColorSquare(Vec2 posA, Vec2 posB, int r, int g, int b, int a, List<Quad> exclusions)
	{
		Vec2 topLeft = new Vec2(posA.x, posB.y);
		Vec2 topRight = posB;
		Vec2 botRight = new Vec2(posB.x, posA.y);
		Vec2 botLeft = posA;
		
		drawBlockColorSquare(new Quad(topLeft, topRight, botRight, botLeft), r, g, b, a, exclusions);
	}
	
	public static void drawBlockColorSquare(Quad quadIn, int r, int g, int b, int a, List<Quad> exclusions)
	{
		List<Quad> totalQuads = Lists.newArrayList();
		totalQuads.add(quadIn);
		
		drawBlockColorSquare(totalQuads, r, g, b, a, exclusions);
	}
	
	public static void drawBlockColorSquare(List<Quad> totalQuads, int r, int g, int b, int a, List<Quad> exclusions)
	{
		totalQuads = splitQuadsRecursive(totalQuads, exclusions);
		if(!totalQuads.isEmpty())
			totalQuads.forEach((finalisedQuad) -> drawBlockColorSquare(finalisedQuad.a(), finalisedQuad.b(), finalisedQuad.c(), finalisedQuad.d(), r, g, b, a));
	}
	
	private static List<Quad> splitQuadsRecursive(List<Quad> quadsToSplit, List<Quad> exclusions)
	{
		if(exclusions.isEmpty())
			return quadsToSplit;
		else if(quadsToSplit.isEmpty())
			return Lists.newArrayList();
		
		List<Quad> quadsSplit = Lists.newArrayList();
		quadsSplit.addAll(quadsToSplit);
		quadsSplit.removeIf((quad) -> quad.isUndrawable());
		
		if(!quadsSplit.isEmpty())
			for(Quad exclusion : exclusions)
			{
				List<Quad> nextSet = excludeQuads(exclusion, quadsSplit);
				nextSet.removeIf((quad) -> quad.isUndrawable());
				
				quadsSplit.clear();
				quadsSplit.addAll(nextSet);
			}
		
		return quadsSplit;
	}
	
	private static List<Quad> excludeQuads(Quad exclusion, List<Quad> quadsSplit)
	{
		// Step 1: Remove all quads entirely within the exclusion
		// Step 2: Split quads bisected by the exclusion
		// Step 3: Repeat until no quads are split or removed
		
		boolean actionTaken = false;
		List<Quad> nextSet = Lists.newArrayList();
		for(Quad quad : quadsSplit)
		{
			// Skip the quad if it is entirely inside the exclusion
			if(exclusion.entirelyOverlaps(quad))
			{
				actionTaken = true;
				continue;
			}
			
			Line intersectingLine = quad.intersects(exclusion);
			// If there's an intersection, split the quad along it and add any resulting quads
			if(intersectingLine != null)
			{
				List<Quad> split = quad.splitAlong(intersectingLine);
				if(split.size() > 1)
				{
					actionTaken = true;
					for(Quad splitQuad : quad.splitAlong(intersectingLine))
						if(!exclusion.entirelyOverlaps(splitQuad))
							nextSet.add(splitQuad);
				}
			}
			else	// If there's no intersection, just add the entire quad
				nextSet.add(quad);
		}
		
		nextSet.removeIf((quad) -> quad.isUndrawable());
		return actionTaken ? excludeQuads(exclusion, nextSet) : nextSet;
	}
	
	/** Draws a coloured square into the GUI */
	public static void drawBlockColorSquare(Vec2 vecA, Vec2 vecB, Vec2 vecC, Vec2 vecD, int r, int g, int b, int a)
	{
	    RenderSystem.setShader(GameRenderer::getPositionColorShader);
	    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, (buffer) -> 
		{
			buffer.vertex(vecA.x, vecA.y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(vecB.x, vecB.y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(vecC.x, vecC.y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(vecD.x, vecD.y, 0).color(r, g, b, a).endVertex();
		});
	}
	
	public static void drawBlockColorSquare(PoseStack matrixStack, MultiBufferSource bufferSource, Quad quad, int r, int g, int b, int a)
	{
		Vec2 vecA = quad.a();
		Vec2 vecB = quad.b();
		Vec2 vecC = quad.c();
		Vec2 vecD = quad.d();
		
		drawBlockColorSquare(matrixStack, bufferSource, vecA, vecB, vecC, vecD, r, g, b, a);
	}
	
	/** Draws a coloured square into the world */
	public static void drawBlockColorSquare(PoseStack matrixStack, MultiBufferSource bufferSource, Vec2 vecA, Vec2 vecB, Vec2 vecC, Vec2 vecD, int r, int g, int b, int a)
	{
		VertexConsumer buffer = bufferSource.getBuffer(RenderType.lightning());
		Matrix4f matrix = matrixStack.last().pose();
		
		matrixStack.pushPose();
			buffer.vertex(matrix, vecA.x, vecA.y, 0F).color(r, g, b, a).endVertex();
			buffer.vertex(matrix, vecB.x, vecB.y, 0F).color(r, g, b, a).endVertex();
			buffer.vertex(matrix, vecD.x, vecD.y, 0F).color(r, g, b, a).endVertex();
			buffer.vertex(matrix, vecC.x, vecC.y, 0F).color(r, g, b, a).endVertex();
		matrixStack.popPose();
		
		matrixStack.pushPose();
			buffer.vertex(matrix, vecC.x, vecC.y, 0F).color(r, g, b, a).endVertex();
			buffer.vertex(matrix, vecD.x, vecD.y, 0F).color(r, g, b, a).endVertex();
			buffer.vertex(matrix, vecB.x, vecB.y, 0F).color(r, g, b, a).endVertex();
			buffer.vertex(matrix, vecA.x, vecA.y, 0F).color(r, g, b, a).endVertex();
		matrixStack.popPose();
	}
	
	public static void drawOutlineCircle(Vec2 posA, float radius, float thickness, List<Quad> exclusions)
	{
		drawOutlineCircle(posA, radius, thickness, 255, 255, 255, 255, exclusions);
	}
	
	/** Draws a hollow circular shape into the GUI */
	public static void drawOutlineCircle(Vec2 position, float radius, float thickness, int r, int g, int b, int a, List<Quad> exclusions)
	{
		int resolution = (int)((2 * Math.PI * radius) / CIRCLE_UNIT);
		Vec2 offsetOut = new Vec2(radius + thickness / 2, 0);
		Vec2 offsetIn = new Vec2(radius - thickness / 2, 0);
		float turn = 360F / resolution;
		for(int i=0; i<resolution; i++)
			RenderUtils.drawBlockColorSquare(
					new Quad(position.add(offsetOut), position.add(offsetIn), position.add(offsetIn = M19Utils.rotate(offsetIn, turn)), position.add(offsetOut = M19Utils.rotate(offsetOut, turn))), 
					r, g, b, a, exclusions);
	}
	
	/** Draws a hollow circular shape in the world */
	public static void drawOutlineCircle(PoseStack matrixStack, MultiBufferSource bufferSource, Vec2 position, float radius, float thickness, int r, int g, int b, int a)
	{
		int resolution = (int)((2 * Math.PI * radius) / CIRCLE_UNIT);
		Vec2 offsetOut = new Vec2(radius + thickness / 2, 0);
		Vec2 offsetIn = new Vec2(radius - thickness / 2, 0);
		float turn = 360F / resolution;
		for(int i=0; i<resolution; i++)
		{
			Vec2 topLeft = position.add(offsetOut);
			Vec2 topRight = position.add(offsetIn);
			Vec2 botRight = position.add(offsetIn = M19Utils.rotate(offsetIn, turn));
			Vec2 botLeft = position.add(offsetOut = M19Utils.rotate(offsetOut, turn));
			
			RenderUtils.drawBlockColorSquare(matrixStack, bufferSource, topLeft, topRight, botRight, botLeft, r, g, b, a);
		}
	}
	
	public static void draw(VertexFormat.Mode drawMode, VertexFormat format, Consumer<BufferBuilder> func)
	{
		draw(drawMode, format, bufferBuilder ->
		{
			func.accept(bufferBuilder);
			return null;
		});
	}
	
	private static <R> R draw(VertexFormat.Mode drawMode, VertexFormat format, Function<BufferBuilder, R> func)
	{
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuilder();
		buffer.begin(drawMode, format);
		R result = func.apply(buffer);
		tesselator.end();
		return result;
	}
}
