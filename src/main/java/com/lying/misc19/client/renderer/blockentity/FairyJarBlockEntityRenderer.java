package com.lying.misc19.client.renderer.blockentity;

import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;

import com.lying.misc19.blocks.entity.FairyJarBlockEntity;
import com.lying.misc19.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class FairyJarBlockEntityRenderer implements BlockEntityRenderer<FairyJarBlockEntity>
{
	private static final Minecraft mc = Minecraft.getInstance();
	private static final Vec3 VEC_OFFSET = FairyJarBlockEntity.ORB_OFFSET;
	private static final ResourceLocation TEXTURE = new ResourceLocation("forge","textures/white.png");
	private static final ResourceLocation BLINK_TEX = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/fairy_jar/blink.png");
	private static final int CIRCLE_RESOLUTION = 40;
	private static final List<Pair<Vec3,Vec3>> TRIANGLES = Lists.newArrayList();
	
	public FairyJarBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }
	
	public void render(FairyJarBlockEntity fairyTile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int p_112311_, int p_112312_)
	{
		Vec3 eyePos = mc.gameRenderer.getMainCamera().getPosition();
		BlockPos tilePos = fairyTile.getBlockPos();
		Vec3 jarPos = new Vec3(tilePos.getX(), tilePos.getY(), tilePos.getZ()).add(VEC_OFFSET);
		Vec3 dirToJar = jarPos.subtract(eyePos).normalize();
		
		renderExpression(fairyTile, dirToJar, matrixStack, bufferSource, partialTicks, p_112312_);
		
		renderOrb(fairyTile, matrixStack, bufferSource, dirToJar);
	}
	
	private void renderExpression(FairyJarBlockEntity fairyTile, Vec3 dirToJar, PoseStack matrixStack, MultiBufferSource bufferSource, float partialTicks, int p_112312_)
	{
		ResourceLocation expressionTex = fairyTile.isBlinking() ? BLINK_TEX : fairyTile.getExpression().getTexture();
		
		matrixStack.pushPose();
			matrixStack.translate(VEC_OFFSET.x, VEC_OFFSET.y, VEC_OFFSET.z);
			matrixStack.scale(0.7F, 0.7F, 0.7F);
			matrixStack.mulPose(Vector3f.YP.rotationDegrees(fairyTile.getYaw(partialTicks)));
			matrixStack.mulPose(Vector3f.XP.rotationDegrees(fairyTile.getPitch(partialTicks)));
			Matrix4f matrix = matrixStack.last().pose();
			VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(expressionTex));
			Vec3 a = new Vec3(-0.5D, -0.5D, 0.5D);
			Vec3 b = new Vec3(0.5D, -0.5D, 0.5D);
			Vec3 c = new Vec3(0.5D, 0.5D, 0.5D);
			Vec3 d = new Vec3(-0.5D, 0.5D, 0.5D);
			
			buffer.vertex(matrix, (float)a.x, (float)a.y, (float)a.z).color(255, 255, 255, 255).uv(0F, 1F).uv2(255).endVertex();
			buffer.vertex(matrix, (float)b.x, (float)b.y, (float)b.z).color(255, 255, 255, 255).uv(1F, 1F).uv2(255).endVertex();
			buffer.vertex(matrix, (float)c.x, (float)c.y, (float)c.z).color(255, 255, 255, 255).uv(1F, 0F).uv2(255).endVertex();
			buffer.vertex(matrix, (float)d.x, (float)d.y, (float)d.z).color(255, 255, 255, 255).uv(0F, 0F).uv2(255).endVertex();
			
			buffer.vertex(matrix, (float)d.x, (float)d.y, (float)d.z).color(255, 255, 255, 255).uv(0F, 0F).uv2(255).endVertex();
			buffer.vertex(matrix, (float)c.x, (float)c.y, (float)c.z).color(255, 255, 255, 255).uv(1F, 0F).uv2(255).endVertex();
			buffer.vertex(matrix, (float)b.x, (float)b.y, (float)b.z).color(255, 255, 255, 255).uv(1F, 1F).uv2(255).endVertex();
			buffer.vertex(matrix, (float)a.x, (float)a.y, (float)a.z).color(255, 255, 255, 255).uv(0F, 1F).uv2(255).endVertex();
		matrixStack.popPose();
	}
	
	private void renderOrb(FairyJarBlockEntity fairyTile, PoseStack matrixStack, MultiBufferSource bufferSource, Vec3 dirToJar)
	{
		Matrix4f matrix = matrixStack.last().pose();
		VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(TEXTURE));
		matrixStack.pushPose();
			float yaw = (float)Math.atan2(dirToJar.x, dirToJar.z);
			float pitch = (float)Math.asin(dirToJar.y);
			TRIANGLES.forEach((pairing) -> 
			{
				Vec3 b = pairing.getLeft().xRot(pitch).yRot(yaw).add(VEC_OFFSET);
				Vec3 c = pairing.getRight().xRot(pitch).yRot(yaw).add(VEC_OFFSET);
				drawTriangle(matrix, VEC_OFFSET, b, c, buffer);
			});
		matrixStack.popPose();
	}
	
	private void drawTriangle(Matrix4f matrix, Vec3 a, Vec3 b, Vec3 c, VertexConsumer buffer)
	{
		buffer.vertex(matrix, (float)a.x, (float)a.y, (float)a.z).color(0, 0, 0, 255).uv(1F, 0F).uv2(255).endVertex();
		buffer.vertex(matrix, (float)b.x, (float)b.y, (float)b.z).color(0, 0, 0, 255).uv(0F, 1F).uv2(255).endVertex();
		buffer.vertex(matrix, (float)c.x, (float)c.y, (float)c.z).color(0, 0, 0, 255).uv(1F, 1F).uv2(255).endVertex();
		buffer.vertex(matrix, (float)a.x, (float)a.y, (float)a.z).color(0, 0, 0, 255).uv(1F, 0F).uv2(255).endVertex();
	}
	
	static
	{
		double radians = Math.toRadians(360F / CIRCLE_RESOLUTION);
		Vec3 vecB = new Vec3(FairyJarBlockEntity.ORB_RADIUS, 0, 0);
		Vec3 vecC = vecB.zRot((float)radians);
		for(int i=0; i<CIRCLE_RESOLUTION; i++)
		{
			TRIANGLES.add(Pair.of(vecB, vecC));
			vecB = vecC;
			vecC = vecC.zRot((float)radians);
		}
	}
}
