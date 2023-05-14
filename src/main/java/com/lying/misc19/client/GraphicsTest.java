package com.lying.misc19.client;

import com.lying.misc19.client.renderer.RenderUtils;
import com.lying.misc19.reference.Reference;
import com.lying.misc19.utility.M19Utils;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

/** Testing replacing the quads approach of Canvas with a graphical approach to hopefully reduce lag in the editor */
public class GraphicsTest 
{
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(Reference.ModInfo.MOD_ID, "magic/1");
	Minecraft mc = Minecraft.getInstance();
	NativeImage image = new NativeImage(480, 480, false);
	DynamicTexture tex = new DynamicTexture(image);
	
	boolean dirty = true;
	
	public GraphicsTest()
	{
		mc.textureManager.register(TEXTURE_LOCATION, this.tex);
		drawCircle(240, 240, 100);
		drawSquare(140, 140, 190, 200);
	}
	
	public int width() { return image.getWidth(); }
	public int height() { return image.getHeight(); }
	public int area() { return width() * height(); }
	
	public void render(int screenX, int screenY, PoseStack matrixStack)
	{
		if(dirty)
		{
			this.tex.upload();
			dirty = false;
		}
		
		Vec2[] vertices = new Vec2[]{
				new Vec2(screenX - width() / 2, screenY - height() / 2),
				new Vec2(screenX + width() / 2, screenY - height() / 2),
				new Vec2(screenX + width() / 2, screenY + height() / 2),
				new Vec2(screenX - width() / 2, screenY + height() / 2)};
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
	    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX, (buffer) -> 
		{
			buffer.vertex(vertices[0].x, vertices[0].y, 0).uv(0F, 0F).endVertex();
			buffer.vertex(vertices[3].x, vertices[3].y, 0).uv(0F, 1F).endVertex();
			buffer.vertex(vertices[2].x, vertices[2].y, 0).uv(1F, 1F).endVertex();
			buffer.vertex(vertices[1].x, vertices[1].y, 0).uv(1F, 0F).endVertex();
		});
	}
	
	public void drawCircle(int x, int y, int radius)
	{
		Vec2 offset = new Vec2(0, radius);
		double rads = Math.toRadians(1D);
		double cos = Math.cos(rads);
		double sin = Math.sin(rads);
		for(int i=0; i<360; i++)
		{
			Vec2 pos = new Vec2(x, y).add(offset);
			Vec2 posB = new Vec2(x, y).add(offset = M19Utils.rotate(offset, cos, sin));
			drawLine(pos, posB);
		}
	}
	
	public void drawLine(Vec2 a, Vec2 b)
	{
		Vec2 dir = b.add(a.negated());
		double len = dir.length();
		dir = dir.normalized();
		
		for(int i=0; i<len; i++)
		{
			Vec2 pos = a.add(dir.scale(i));
			setPixel((int)pos.x, (int)pos.y);
		}
	}
	
	public void drawSquare(int minX, int minY, int maxX, int maxY)
	{
		Vec2 xy = new Vec2(minX, minY);
		Vec2 Xy = new Vec2(maxX, minY);
		Vec2 XY = new Vec2(maxX, maxY);
		Vec2 xY = new Vec2(minX, maxY);
		
		drawLine(xy, Xy);
		drawLine(Xy, XY);
		drawLine(XY, xY);
		drawLine(xY, xy);
	}
	
	public void setPixel(int x, int y)
	{
		if(x < 0 || x > width() || y < 0 || y > height())
			return;
		
		image.setPixelRGBA(x, y, -1);
		this.dirty = true;
	}
}
