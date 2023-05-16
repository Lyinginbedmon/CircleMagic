package com.lying.misc19.client.renderer.blockentity;

import com.lying.misc19.blocks.entity.InscriptionBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class InscriptionBlockEntityRenderer implements BlockEntityRenderer<InscriptionBlockEntity>
{
	public InscriptionBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }
	
	public void render(InscriptionBlockEntity phantomTile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int p_112311_, int p_112312_)
	{
		if(phantomTile.isValid())
		{
			// TODO If valid block, display ripple texture at floor level
		}
	}
}
