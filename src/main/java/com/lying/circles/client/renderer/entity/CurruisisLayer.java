package com.lying.circles.client.renderer.entity;

import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.client.model.CurruisisModel;
import com.lying.circles.client.renderer.CMModelLayers;
import com.lying.circles.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class CurruisisLayer<T extends Player, M extends HumanoidModel<T>> extends RenderLayer<T, M>
{
	private static final ResourceLocation[] TEXTURES = new ResourceLocation[PlayerData.MAX_CURRUISIS];
	private final CurruisisModel<T> model;
	
	public CurruisisLayer(RenderLayerParent<T, M> rendererIn, EntityModelSet modelSet)
	{
		super(rendererIn);
		this.model = new CurruisisModel<T>(modelSet.bakeLayer(CMModelLayers.CURRUISIS));
	}
	
	public void render(PoseStack p_116951_, MultiBufferSource p_116952_, int p_116953_, T player, float p_116955_, float p_116956_, float p_116957_, float p_116958_, float p_116959_, float p_116960_)
	{
		PlayerData data = PlayerData.getCapability(player);
		if(!data.hasCurruisis() || data.isALich())
			return;
		
		for(int i=1; i<PlayerData.MAX_CURRUISIS; i++)
		{
			p_116951_.pushPose();
				this.getParentModel().copyPropertiesTo(this.model);
				this.model.setupAnim(player, p_116955_, p_116956_, p_116958_, p_116959_, p_116960_);
				this.model.adjustCurruisis(data.getCurruisis(), i);
				VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(p_116952_, RenderType.armorCutoutNoCull(TEXTURES[i-1]), false, false);
				this.model.renderToBuffer(p_116951_, vertexconsumer, p_116953_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			p_116951_.popPose();
		}
	}
	
	static
	{
		for(int i=1; i<PlayerData.MAX_CURRUISIS; i++)
			TEXTURES[i-1] = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/curruisis_"+i+".png");
	}
}
