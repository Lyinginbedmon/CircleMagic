package com.lying.circles.client.renderer.blockentity;

import java.util.Random;

import com.lying.circles.blocks.entity.InscribedBlockEntity;
import com.lying.circles.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class InscribedBlockEntityRenderer implements BlockEntityRenderer<InscribedBlockEntity>
{
	private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/misc/pillar_oscillation.png");
	private static final float GROWTH_RATE = Reference.Values.TICKS_PER_SECOND * 2;
	
	public InscribedBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }
	
	public void render(InscribedBlockEntity inscribedTile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int p_112311_, int p_112312_)
	{
		if(inscribedTile.isValid())
		{
			BlockPos pos = inscribedTile.getBlockPos();
			Random rand = new Random(pos.getX() * pos.getX() + pos.getZ() * pos.getZ());
			float ticks = inscribedTile.renderTicks() + partialTicks + (rand.nextFloat() * 1000F);
			
			float growth = (ticks % GROWTH_RATE) / GROWTH_RATE;
			
			float size = growth * 0.5F * 3F;
			if(size < 1F)
				return;
			
			int alpha = (int)((growth > 0.5F ? (1 - (growth - 0.5F)/0.5F) : 1F) * 255);
			
			matrixStack.pushPose();
				matrixStack.translate(0.5D, -0.9D, 0.5D);
				matrixStack.pushPose();
					VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(FORCEFIELD_LOCATION));
					Matrix4f matrix = matrixStack.last().pose();
					buffer.vertex(matrix, -size, 0, size).color(255, 255, 255, alpha).uv(1F, 0F).uv2(255).endVertex();
					buffer.vertex(matrix, size, 0, size).color(255, 255, 255, alpha).uv(0F, 0F).uv2(255).endVertex();
					buffer.vertex(matrix, size, 0, -size).color(255, 255, 255, alpha).uv(0F, 1F).uv2(255).endVertex();
					buffer.vertex(matrix, -size, 0, -size).color(255, 255, 255, alpha).uv(1F, 1F).uv2(255).endVertex();
				matrixStack.popPose();
			matrixStack.popPose();
		}
	}
}
