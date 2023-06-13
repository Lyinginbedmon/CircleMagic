package com.lying.circles.client.renderer.blockentity;

import com.lying.circles.blocks.entity.StatueBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class StatueBlockEntityRenderer implements BlockEntityRenderer<StatueBlockEntity>
{
	private final EntityRenderDispatcher entityRenderer;
	
	public StatueBlockEntityRenderer(BlockEntityRendererProvider.Context context)
	{
		this.entityRenderer = context.getEntityRenderer();
	}
	
	public void render(StatueBlockEntity treeTile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay)
	{
		Entity statue = treeTile.getStatue(treeTile.getLevel());
		if(statue == null)
			return;
		
		BlockPos pos = treeTile.getBlockPos();
		RandomSource rand = RandomSource.create(pos.getX() * pos.getX() - pos.getZ() * pos.getZ() + pos.getY() * pos.getY());
		matrixStack.pushPose();
			matrixStack.translate(0.5D, 0D, 0.5D);
			matrixStack.mulPose(Vector3f.YP.rotationDegrees(rand.nextInt(360)));
			this.entityRenderer.render(statue, 0, 0, 0, 0, partialTicks, matrixStack, bufferSource, packedLight);
		matrixStack.popPose();
	}
}
