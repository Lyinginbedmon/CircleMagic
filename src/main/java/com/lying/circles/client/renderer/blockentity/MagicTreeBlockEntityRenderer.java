package com.lying.circles.client.renderer.blockentity;

import com.lying.circles.blocks.entity.MagicTreeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

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
	
	public void render(MagicTreeBlockEntity treeTile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay)
	{
		if(treeTile.isEmpty())
			return;
		
		ItemStack stack = treeTile.getItem(0);
		matrixStack.pushPose();
			matrixStack.translate(0.5D, 1.35D, 0.5D);
			matrixStack.pushPose();
				matrixStack.mulPose(Vector3f.YP.rotationDegrees(((float)treeTile.renderTicks() + partialTicks) * 3F));
				float scale = 0.4F;
				matrixStack.scale(scale, scale, scale);
				this.itemRenderer.renderStatic(stack, TransformType.FIXED, packedLight, OverlayTexture.NO_OVERLAY, matrixStack, bufferSource, 0);
			matrixStack.popPose();
		matrixStack.popPose();
	}
}
