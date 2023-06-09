package com.lying.circles.client.renderer.blockentity;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.blocks.entity.FairyJarBlockEntity;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.CMUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class FairyJarBlockEntityRenderer implements BlockEntityRenderer<FairyJarBlockEntity>
{
	private static final Minecraft mc = Minecraft.getInstance();
	private static final Vec3 VEC_OFFSET = FairyJarBlockEntity.ORB_OFFSET;
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("forge","textures/white.png");
	private static final int SPHERE_RESOLUTION = 35;
	private static final List<Quad> QUADS = Lists.newArrayList();
	
	private static final ResourceLocation BLINK_TEX = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/fairy_jar/blink.png");
	
	public FairyJarBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }
	
	public void render(FairyJarBlockEntity fairyTile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn)
	{
		HitResult hitResult = mc.getCameraEntity().pick(8D, 0F, false);
		if(hitResult.getType() == Type.BLOCK && ((BlockHitResult)hitResult).getBlockPos().equals(fairyTile.getBlockPos()) && Minecraft.renderNames())
			renderName(fairyTile, matrixStack, bufferSource, combinedLightIn);
		
		Vec3 eyePos = mc.gameRenderer.getMainCamera().getPosition();
		BlockPos tilePos = fairyTile.getBlockPos();
		Vec3 jarPos = new Vec3(tilePos.getX(), tilePos.getY(), tilePos.getZ()).add(VEC_OFFSET);
		Vec3 dirToJar = jarPos.subtract(eyePos).normalize();
		
		renderOrb(fairyTile, matrixStack, bufferSource, dirToJar);
		renderExpression(fairyTile, matrixStack, bufferSource, partialTicks);
	}
	
	private void renderName(FairyJarBlockEntity fairyTile, PoseStack matrixStack, MultiBufferSource bufferSource, int combinedLightIn)
	{
		MutableComponent displayName = fairyTile.displayName();
        matrixStack.pushPose();
	        matrixStack.translate(FairyJarBlockEntity.ORB_OFFSET.x, FairyJarBlockEntity.ORB_OFFSET.y + 1D, FairyJarBlockEntity.ORB_OFFSET.z);
			matrixStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
	        matrixStack.scale(-0.025F, -0.025F, 0.025F);
			Matrix4f matrix4f = matrixStack.last().pose();
			float opacity = mc.options.getBackgroundOpacity(0.25F);
			int j = (int)(opacity * 255.0F) << 24;
			Font font = mc.font;
	        float width = (float)(-font.width(displayName) / 2);
	        font.drawInBatch(displayName, width, 0F, 553648127, false, matrix4f, bufferSource, true, j, combinedLightIn);
	        font.drawInBatch(displayName, width, 0F, -1, false, matrix4f, bufferSource, false, 0, combinedLightIn);
        matrixStack.popPose();
	}
	
	private void renderExpression(FairyJarBlockEntity fairyTile, PoseStack matrixStack, MultiBufferSource bufferSource, float partialTicks)
	{
		ResourceLocation expressionTex = fairyTile.isBlinking() ? BLINK_TEX : fairyTile.getExpression().getTexture();
		matrixStack.pushPose();
			matrixStack.translate(VEC_OFFSET.x, VEC_OFFSET.y, VEC_OFFSET.z);
			matrixStack.scale(0.45F, 0.45F, 0.45F);
			matrixStack.mulPose(Vector3f.YP.rotationDegrees(Mth.wrapDegrees(fairyTile.getYaw(partialTicks))));
			matrixStack.mulPose(Vector3f.XP.rotationDegrees(Mth.wrapDegrees(fairyTile.getPitch(partialTicks))));
			Matrix4f matrix = matrixStack.last().pose();
			VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(expressionTex));
			buffer.vertex(matrix, -0.5F, -0.5F, 0.5F).color(255, 255, 255, 255).uv(0F, 1F).uv2(255).endVertex();
			buffer.vertex(matrix, 0.5F, -0.5F, 0.5F).color(255, 255, 255, 255).uv(1F, 1F).uv2(255).endVertex();
			buffer.vertex(matrix, 0.5F, 0.5F, 0.5F).color(255, 255, 255, 255).uv(1F, 0F).uv2(255).endVertex();
			buffer.vertex(matrix, -0.5F, 0.5F, 0.5F).color(255, 255, 255, 255).uv(0F, 0F).uv2(255).endVertex();
			
			buffer.vertex(matrix, -0.5F, 0.5F, 0.5F).color(255, 255, 255, 255).uv(0F, 0F).uv2(255).endVertex();
			buffer.vertex(matrix, 0.5F, 0.5F, 0.5F).color(255, 255, 255, 255).uv(1F, 0F).uv2(255).endVertex();
			buffer.vertex(matrix, 0.5F, -0.5F, 0.5F).color(255, 255, 255, 255).uv(1F, 1F).uv2(255).endVertex();
			buffer.vertex(matrix, -0.5F, -0.5F, 0.5F).color(255, 255, 255, 255).uv(0F, 1F).uv2(255).endVertex();
		matrixStack.popPose();
	}
	
	private void renderOrb(FairyJarBlockEntity fairyTile, PoseStack matrixStack, MultiBufferSource bufferSource, Vec3 dirToJar)
	{
		Matrix4f matrix = matrixStack.last().pose();
		VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(TEXTURE));
		matrixStack.pushPose();
			QUADS.forEach((quad) -> quad.scale(FairyJarBlockEntity.ORB_RADIUS).move(FairyJarBlockEntity.ORB_OFFSET).addToBuffer(matrix, buffer, 0, 0, 0));
		matrixStack.popPose();
	}
	
	private static class Quad
	{
		public final Vec3 topLeft, topRight, botRight, botLeft;
		public Quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d)
		{
			topLeft = a;
			topRight = b;
			botRight = c;
			botLeft = d;
		}
		
		public Quad scale(double scale)
		{
			return new Quad(topLeft.scale(scale), topRight.scale(scale), botRight.scale(scale), botLeft.scale(scale));
		}
		
		public Quad move(Vec3 offset)
		{
			return new Quad(topLeft.add(offset), topRight.add(offset), botRight.add(offset), botLeft.add(offset));
		}
		
		public void addToBuffer(Matrix4f matrix, VertexConsumer buffer, int r, int g, int b)
		{
			buffer.vertex(matrix, (float)topLeft.x, (float)topLeft.y, (float)topLeft.z).color(r, g, b, 255).uv(0F, 1F).uv2(255).endVertex();
			buffer.vertex(matrix, (float)topRight.x, (float)topRight.y, (float)topRight.z).color(r, g, b, 255).uv(1F, 1F).uv2(255).endVertex();
			buffer.vertex(matrix, (float)botRight.x, (float)botRight.y, (float)botRight.z).color(r, g, b, 255).uv(1F, 0F).uv2(255).endVertex();
			buffer.vertex(matrix, (float)botLeft.x, (float)botLeft.y, (float)botLeft.z).color(r, g, b, 255).uv(0F, 0F).uv2(255).endVertex();
		}
	}
	
	static
	{
		// Generate a sphere of R = 1
		double cos = Math.cos(Math.toRadians(360D / SPHERE_RESOLUTION));
		double sin = Math.sin(Math.toRadians(360D / SPHERE_RESOLUTION));
		for(float j=0; j<SPHERE_RESOLUTION; j++)
		{
			/*
			 * Radius of spherical cap = sqrt(h(2R-h)) where h = cap height
			 */
			float botH = (j / SPHERE_RESOLUTION) * 2F;
			float altBot = botH - 1F;
			float a = (float)Math.sqrt(botH * (2F - botH));
			Vec2 radiusBot = new Vec2(a, 0F);
			
			float topH = ((j + 1) / SPHERE_RESOLUTION) * 2F;
			float altTop = topH - 1F;
			a = (float)Math.sqrt(topH * (2F - topH));
			Vec2 radiusTop = new Vec2(a, 0F);
			
			for(int i=0; i<SPHERE_RESOLUTION; i++)
			{
				Vec2 botLeft = radiusBot;
				Vec2 botRight = radiusBot = CMUtils.rotate(radiusBot, cos, sin);
				Vec2 topLeft = radiusTop;
				Vec2 topRight = radiusTop = CMUtils.rotate(radiusTop, cos, sin);
				
				QUADS.add(new Quad(
						new Vec3(topLeft.x, altTop, topLeft.y),
						new Vec3(topRight.x, altTop, topRight.y),
						new Vec3(botRight.x, altBot, botRight.y),
						new Vec3(botLeft.x, altBot, botLeft.y)));
			}
		}
	}
}
