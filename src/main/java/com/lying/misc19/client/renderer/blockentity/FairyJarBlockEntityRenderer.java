package com.lying.misc19.client.renderer.blockentity;

import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;

import com.lying.misc19.blocks.entity.FairyJarBlockEntity;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class FairyJarBlockEntityRenderer implements BlockEntityRenderer<FairyJarBlockEntity>
{
	private static final Minecraft mc = Minecraft.getInstance();
	private static final ResourceLocation TEXTURE = new ResourceLocation("forge","textures/white.png");
	private static final int CIRCLE_RES = 40;
	private static final double RADIUS = 0.2D;
	private static final List<Pair<Vec3,Vec3>> TRIANGLES = Lists.newArrayList();
	private static final float ARC_DEG = 360F / CIRCLE_RES;
	private static final double ARC_RADS = Math.toRadians(ARC_DEG);
	
	private static final Vec3 VEC_OFFSET = new Vec3(0.5D, RADIUS, 0.5D);
	
	public FairyJarBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }
	
	public void render(FairyJarBlockEntity fairyTile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int p_112311_, int p_112312_)
	{
		Vec3 eyePos = mc.gameRenderer.getMainCamera().getPosition();
		BlockPos tilePos = fairyTile.getBlockPos();
		Vec3 jarPos = new Vec3(tilePos.getX(), tilePos.getY(), tilePos.getZ()).add(VEC_OFFSET);
		Vec3 dirToJar = jarPos.subtract(eyePos).normalize();
		
		renderExpression(fairyTile, dirToJar, matrixStack, bufferSource, p_112312_);
		
		renderOrb(fairyTile, matrixStack, bufferSource, dirToJar);
	}
	
	private void renderExpression(FairyJarBlockEntity fairyTile, Vec3 dirToJar, PoseStack matrixStack, MultiBufferSource bufferSource, int p_112312_)
	{
		Font font = mc.font;
		matrixStack.pushPose();
			matrixStack.translate(0.5D, 1.3D, 0.5D);
			matrixStack.scale(-0.025F, -0.025F, 0.025F);
			matrixStack.mulPose(Vector3f.YP.rotation((float)-Math.atan2(dirToJar.x, dirToJar.z)));
			float f1 = mc.options.getBackgroundOpacity(0.25F);
			int j = (int)(f1 * 255.0F) << 24;
			
			Component emote = Component.literal(fairyTile.getExpression().getSerializedName());
			Matrix4f matrix4f = matrixStack.last().pose();
			float width = (float)(-font.width(emote) / 2);
			font.drawInBatch(emote, width, 0, 553648127, false, matrix4f, bufferSource, true, j, p_112312_);
			font.drawInBatch(emote, width, 0, 1, false, matrix4f, bufferSource, false, j, p_112312_);
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
				Vec3 b = pairing.getLeft().xRot(pitch).yRot(yaw);
				Vec3 c = pairing.getRight().xRot(pitch).yRot(yaw);
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
		Vec3 vecB = new Vec3(RADIUS, 0, 0);
		Vec3 vecC = vecB.zRot((float)ARC_RADS);
		for(int i=0; i<CIRCLE_RES; i++)
		{
			TRIANGLES.add(Pair.of(vecB.add(VEC_OFFSET), vecC.add(VEC_OFFSET)));
			vecB = vecC;
			vecC = vecC.zRot((float)ARC_RADS);
		}
	}
}
