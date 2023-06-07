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
