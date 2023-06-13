package com.lying.circles.client.renderer.entity;

import com.lying.circles.client.renderer.CMModelLayers;
import com.lying.circles.entities.StatueEntity;
import com.lying.circles.reference.Reference;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class StatueRenderer extends LivingEntityRenderer<StatueEntity, HumanoidModel<StatueEntity>>
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/statue.png");
	
	public StatueRenderer(Context context)
	{
		super(context, new HumanoidModel<StatueEntity>(context.bakeLayer(CMModelLayers.STATUE)), 0F);
	}
	
	public ResourceLocation getTextureLocation(StatueEntity statue)
	{
		return TEXTURE;
	}
}
