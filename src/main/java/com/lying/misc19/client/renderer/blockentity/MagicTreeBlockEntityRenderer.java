package com.lying.misc19.client.renderer.blockentity;

import com.lying.misc19.blocks.entity.MagicTreeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

public class MagicTreeBlockEntityRenderer implements BlockEntityRenderer<MagicTreeBlockEntity>
{
	private final ItemRenderer itemRenderer;
	
	public MagicTreeBlockEntityRenderer(BlockEntityRendererProvider.Context context)
	{
		this.itemRenderer = Minecraft.getInstance().getItemRenderer();
	}
	
	public void render(MagicTreeBlockEntity treeTile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int p_112311_, int p_112312_)
	{
		if(treeTile.isEmpty())
			return;
		
		ItemStack stack = treeTile.getItem(0);
		matrixStack.pushPose();
			matrixStack.translate(0.5D, 1.5D, 0.5D);
			matrixStack.pushPose();
				float scale = 0.5F;
				matrixStack.scale(scale, scale, scale);
				this.itemRenderer.renderStatic(stack, TransformType.FIXED, p_112311_, OverlayTexture.NO_OVERLAY, matrixStack, bufferSource, 0);
			matrixStack.popPose();
		matrixStack.popPose();
	}
}
