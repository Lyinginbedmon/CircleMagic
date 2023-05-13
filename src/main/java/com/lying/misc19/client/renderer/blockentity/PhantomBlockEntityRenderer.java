package com.lying.misc19.client.renderer.blockentity;

import java.util.List;

import javax.annotation.Nullable;

import com.lying.misc19.blocks.entity.PhantomBlockEntity;
import com.lying.misc19.init.M19Blocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class PhantomBlockEntityRenderer implements BlockEntityRenderer<PhantomBlockEntity>
{
	private static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(M19Blocks.PHANTOM_CUBE.getId(), "");
	private final RandomSource rand = RandomSource.create();
	private final BlockRenderDispatcher blockRenderer;
	private static final double timeScale = 0.05D;
	private static final double randScale = 0.1D;
	
	public PhantomBlockEntityRenderer(BlockEntityRendererProvider.Context context)
	{
		this.blockRenderer = context.getBlockRenderDispatcher();
	}
	
	public void render(PhantomBlockEntity phantomTile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int p_112311_, int p_112312_)
	{
		rand.setSeed(phantomTile.getBlockPos().getX() * phantomTile.getBlockPos().getY() * phantomTile.getBlockPos().getZ());
		
		double xOff = Math.sin(phantomTile.renderTicks() * timeScale + rand.nextDouble()) * randScale;
		double yOff = Math.sin(phantomTile.renderTicks() * timeScale + rand.nextDouble()) * randScale;
		double zOff = Math.sin(phantomTile.renderTicks() * timeScale + rand.nextDouble()) * randScale;
		
		matrixStack.pushPose();
			matrixStack.translate(0.5D, 0.5D, 0.5D);
			matrixStack.pushPose();
				matrixStack.scale(0.8F, 0.8F, 0.8F);
				matrixStack.translate(-0.5D, -0.5D, -0.5D);
				matrixStack.translate(xOff, yOff, zOff);
				ModelManager modelmanager = this.blockRenderer.getBlockModelShaper().getModelManager();
				BakedModel model = modelmanager.getModel(MODEL_LOCATION);
				renderModel(matrixStack.last(), bufferSource.getBuffer(RenderType.translucent()), phantomTile.getBlockState(), model, 1F, 1F, 1F, 0.5F, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, null, RenderType.translucent());
			matrixStack.popPose();
		matrixStack.popPose();
	}
	
	public void renderModel(PoseStack.Pose pose, VertexConsumer consumer, @Nullable BlockState state, BakedModel model, float red, float green, float blue, float alpha, int packedLight, int packedOverlay, ModelData modelData, RenderType renderType)
	{
		RandomSource random = RandomSource.create();
		for(Direction direction : Direction.values())
		{
			random.setSeed(42L);
			renderQuadList(pose, consumer, red, green, blue, alpha, model.getQuads(state, direction, random, modelData, renderType), packedLight, packedOverlay);
		}
		
		random.setSeed(42L);
		renderQuadList(pose, consumer, red, green, blue, alpha, model.getQuads(state, null, random, modelData, renderType), packedLight, packedOverlay);
	}
	
	private static void renderQuadList(PoseStack.Pose pose, VertexConsumer consumer, float red, float green, float blue, float alpha, List<BakedQuad> quads, int packedLight, int packedOverlay)
	{
		for (BakedQuad quad : quads)
		{
			float f;
			float f1;
			float f2;
			if(quad.isTinted())
			{
				f = Mth.clamp(red, 0.0F, 1.0F);
				f1 = Mth.clamp(green, 0.0F, 1.0F);
				f2 = Mth.clamp(blue, 0.0F, 1.0F);
			}
			else
			{
				f = 1.0F;
				f1 = 1.0F;
				f2 = 1.0F;
			}
			
			consumer.putBulkData(pose, quad, f, f1, f2, alpha, packedLight, packedOverlay, true);
		}
	}
}
