package com.lying.circles.client.renderer.entity;

import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.client.model.LichModel;
import com.lying.circles.client.renderer.CMModelLayers;
import com.lying.circles.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class LichLayer<T extends Player, M extends HumanoidModel<T>> extends RenderLayer<T, M>
{
	private static final RenderType RENDER_TYPE = RenderType.eyes(new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/lich.png"));
	// FIXME Lich glow model should fit inside conventional player model
	private final LichModel<T> glowModel;
	
	public LichLayer(RenderLayerParent<T, M> parent, EntityModelSet modelSet)
	{
		super(parent);
		this.glowModel = new LichModel<T>(modelSet.bakeLayer(CMModelLayers.LICH));
	}
	
	public void render(PoseStack matrixStack, MultiBufferSource bufferSource, int p_117351_, T player, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_)
	{
		if(!PlayerData.isLich(player))
			return;
		
		VertexConsumer vertexconsumer = bufferSource.getBuffer(RENDER_TYPE);
		this.getParentModel().copyPropertiesTo(this.glowModel);
		this.glowModel.prepareMobModel(player, p_117353_, p_117354_, p_117355_);
		this.glowModel.setupAnim(player, p_117353_, p_117354_, p_117356_, p_117357_, p_117358_);
		this.glowModel.renderToBuffer(matrixStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}
}
