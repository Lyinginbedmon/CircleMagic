package com.lying.misc19.client.renderer.blockentity;

import com.lying.misc19.blocks.entity.FairyJarBlockEntity;
import com.lying.misc19.utility.M19Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class FairyJarBlockEntityRenderer implements BlockEntityRenderer<FairyJarBlockEntity>
{
	private static final Minecraft mc = Minecraft.getInstance();
	private static final int CIRCLE_RES = 64;
	private static final float turn = 360F / CIRCLE_RES;
	private static final double rads = Math.toRadians(turn);
	private static final double cos = Math.cos(rads);
	private static final double sin = Math.sin(rads);
	
	private static final Vec3 VEC_OFFSET = new Vec3(0.5D, 0.2D, 0.5D);
	
	public FairyJarBlockEntityRenderer(BlockEntityRendererProvider.Context context)
	{
		
	}
	
	public void render(FairyJarBlockEntity fairyTile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int p_112311_, int p_112312_)
	{
		Vec3 eyePos = mc.gameRenderer.getMainCamera().getPosition();
		BlockPos tilePos = fairyTile.getBlockPos();
		Vec3 jarPos = new Vec3(tilePos.getX() + VEC_OFFSET.x, tilePos.getY() + VEC_OFFSET.y, tilePos.getZ() + VEC_OFFSET.z);
		Vec3 toEye = eyePos.subtract(jarPos).normalize();
		
		Font font = mc.font;
		matrixStack.pushPose();
			matrixStack.translate(0.5D, 1.3D, 0.5D);
			matrixStack.scale(-0.025F, -0.025F, 0.025F);
			matrixStack.mulPose(Vector3f.YP.rotation((float)(Math.atan2(toEye.x, toEye.z) - Math.toRadians(90D))));
			float f1 = mc.options.getBackgroundOpacity(0.25F);
			int j = (int)(f1 * 255.0F) << 24;
			
			Component emote = Component.literal(fairyTile.getExpression().getSerializedName());
			Matrix4f matrix4f = matrixStack.last().pose();
			float width = (float)(-font.width(emote) / 2);
			font.drawInBatch(emote, width, 0, 553648127, false, matrix4f, bufferSource, true, j, p_112312_);
			font.drawInBatch(emote, width, 0, -1, false, matrix4f, bufferSource, false, j, p_112312_);
		matrixStack.popPose();
		
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuilder();
		matrixStack.pushPose();
			matrixStack.translate(VEC_OFFSET.x, VEC_OFFSET.y, VEC_OFFSET.z);
			
			matrixStack.mulPose(Vector3f.YP.rotation((float)Math.atan2(toEye.x, toEye.z)));
			matrixStack.mulPose(Vector3f.XP.rotation((float)Math.asin(-toEye.y)));
			
			matrixStack.pushPose();
				RenderSystem.setShader(GameRenderer::getPositionColorShader);
				RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
				Matrix4f matrix = matrixStack.last().pose();
				Vec2 offset = new Vec2(0.2F, 0F);
				builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
				for(int i=0; i<CIRCLE_RES; i++)
				{
					builder.vertex(matrix, offset.x, offset.y, 0).color(0, 0, 0, 255).endVertex();
					offset = M19Utils.rotate(offset, cos, sin);
					builder.vertex(matrix, offset.x, offset.y, 0).color(0, 0, 0, 255).endVertex();
					builder.vertex(matrix, 0, 0, 0).color(0, 0, 0, 255).endVertex();
				}
				BufferUploader.drawWithShader(builder.end());
			matrixStack.popPose();
		matrixStack.popPose();
	}
}
