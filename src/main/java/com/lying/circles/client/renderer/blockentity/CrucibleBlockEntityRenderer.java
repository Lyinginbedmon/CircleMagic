package com.lying.circles.client.renderer.blockentity;

import java.util.Random;

import com.lying.circles.blocks.ICruciblePart;
import com.lying.circles.blocks.ICruciblePart.PartType;
import com.lying.circles.blocks.entity.CrucibleBlockEntity;
import com.lying.circles.client.renderer.RenderUtils;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.CMUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CrucibleBlockEntityRenderer implements BlockEntityRenderer<CrucibleBlockEntity>
{
	private static final Minecraft mc = Minecraft.getInstance();
	
	private static final int SPACING = CrucibleBlockEntity.SPACING;
	private static int guideTicksVisible = 0;
	
	public CrucibleBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }
	
	public void render(CrucibleBlockEntity crucibleTile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int p_112311_, int p_112312_)
	{
		// Render contained arrangement
		if(crucibleTile.arrangement() != null)
		{
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			matrixStack.pushPose();
				matrixStack.translate(0.5D, 0.5D, 0.5D);
				matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90F));
				matrixStack.pushPose();
					matrixStack.scale(5F, 5F, 5F);
					crucibleTile.getCanvas().drawIntoWorld(matrixStack, bufferSource);
				matrixStack.popPose();
			matrixStack.popPose();
		}
		
		Player player = mc.player;
		if(CMUtils.canSeeMagic(player))
			guideTicksVisible = Math.min(guideTicksVisible + 1, Reference.Values.TICKS_PER_SECOND);
		else if(guideTicksVisible > 0)
			guideTicksVisible--;
		
		// Render pillar guide circles
		if(guideTicksVisible > 0)
		{
			BlockPos tilePos = crucibleTile.getBlockPos();
			renderPillarGuides(tilePos, Math.sqrt(player.distanceToSqr(new Vec3(tilePos.getX() + 0.5D, tilePos.getY(), tilePos.getZ() + 0.5D))), matrixStack, bufferSource);
		}
	}
	
	public static boolean isPillarBlock(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		else if(stack.getItem() instanceof BlockItem)
		{
			Block block = ((BlockItem)stack.getItem()).getBlock();
			if(block instanceof ICruciblePart)
				return ((ICruciblePart)block).partType(null, null, null) == PartType.PILLAR;
		}
		return false;
	}
	
	private void renderPillarGuides(BlockPos tilePos, double playerDist, PoseStack matrixStack, MultiBufferSource bufferSource)
	{
		Random rand = new Random(tilePos.getX() * tilePos.getX() + tilePos.getZ() * tilePos.getZ());
		matrixStack.pushPose();
			matrixStack.translate(0.5D, 0.01D, 0.5D);
			matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90F));
			for(int i=1; i<Math.ceil(16D / SPACING); i++)
			{
				float distance = SPACING * i;
				Vec2 offsetOut = new Vec2(distance + 0.1F / 2, 0);
				Vec2 offsetIn = new Vec2(distance - 0.1F / 2, 0);
				
				float res = (float)Math.ceil((2 * Math.PI * distance) / 0.5F);
				float turn = 360F / res;
				double rads = Math.toRadians(turn);
				double cos = Math.cos(rads);
				double sin = Math.sin(rads);
				
				double opacity = playerDist <= distance ? distance - playerDist : playerDist - distance;
				opacity = Mth.clamp(1 - (opacity / SPACING), 0D, 1D);
				if(opacity == 0D)
					continue;
				
				opacity *= Mth.clamp((double)guideTicksVisible / Reference.Values.TICKS_PER_SECOND, 0, 1);
				int alpha = (int)(255 * opacity);
				for(int j=0; j<res; j++)
				{
					Vec2 topLeft = offsetOut;
					Vec2 topRight = offsetIn;
					Vec2 botRight = offsetIn = CMUtils.rotate(offsetIn, cos, sin);
					Vec2 botLeft = offsetOut = CMUtils.rotate(offsetOut, cos, sin);
					
					double brightness = (rand.nextDouble() - 0.5D) * 60;
					int r = (int)Mth.clamp(0, 0, 255);
					int g = (int)Mth.clamp(125 + brightness, 0, 255);
					int b = (int)Mth.clamp(255 + brightness, 0, 255);
					RenderUtils.drawBlockColorSquare(matrixStack, bufferSource, topLeft, topRight, botRight, botLeft, r, g, b, alpha);
				}
			}
		matrixStack.popPose();
	}
}
