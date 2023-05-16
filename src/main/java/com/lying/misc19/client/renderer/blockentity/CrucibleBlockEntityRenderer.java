package com.lying.misc19.client.renderer.blockentity;

import com.lying.misc19.blocks.entity.CrucibleBlockEntity;
import com.lying.misc19.client.renderer.ComponentRenderers;
import com.lying.misc19.magic.ISpellComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class CrucibleBlockEntityRenderer implements BlockEntityRenderer<CrucibleBlockEntity>
{
	public CrucibleBlockEntityRenderer(BlockEntityRendererProvider.Context context)
	{
		
	}
	
	public boolean shouldRenderOffScreen(CrucibleBlockEntity tile) { return true; }
	
	public void render(CrucibleBlockEntity phantomTile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int p_112311_, int p_112312_)
	{
		// FIXME Arrangement should render regardless of player view frustrum
		ISpellComponent arrangement = phantomTile.arrangement();
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		matrixStack.pushPose();
			matrixStack.translate(0.5D, 0.5D, 0.5D);
			matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90F));
			matrixStack.pushPose();
				matrixStack.scale(5F, 5F, 5F);
				ComponentRenderers.renderWorld(arrangement, matrixStack, bufferSource);
			matrixStack.popPose();
		matrixStack.popPose();
	}
}
