package com.lying.circles.client.renderer.entity;

import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.client.model.LichModel;
import com.lying.circles.client.model.LichSkullModel;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class LichLayer<T extends Player, M extends HumanoidModel<T>> extends RenderLayer<T, M>
{
	private static final ResourceLocation ENERGY_TEX = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/lich.png");
	private static final ResourceLocation SKULL_TEX = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/lich_skull.png");
	private static final ResourceLocation DECAY_TEX = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/lich_decay.png");
	private final LichModel<T> glowModel;
	private final LichSkullModel skullModel;
	
	public LichLayer(RenderLayerParent<T, M> parent, EntityModelSet modelSet)
	{
		super(parent);
		this.glowModel = new LichModel<T>(modelSet.bakeLayer(CMModelLayers.LICH));
		this.skullModel = new LichSkullModel(modelSet.bakeLayer(CMModelLayers.LICH_SKULL));
	}
	
	public void render(PoseStack matrixStack, MultiBufferSource bufferSource, int bakedLight, T player, float p_117353_, float p_117354_, float partialTicks, float p_117356_, float p_117357_, float p_117358_)
	{
		if(!PlayerData.isLich(player))
			return;
		
		renderSkull(matrixStack, bufferSource, 180F, p_117353_, bakedLight);
		renderEnergy(matrixStack, bufferSource, bakedLight, player, partialTicks);
		renderSkin(matrixStack, bufferSource, bakedLight, player, partialTicks);
	}
	
	private void renderSkin(PoseStack matrixStack, MultiBufferSource buffersource, int bakedLight, T player, float partialTicks)
	{
		// FIXME Fix transparent pixels in player skin displaying as opaque white 
		matrixStack.pushPose();
			M model = getParentModel();
			model.setAllVisible(true);
			boolean flag = player.hurtTime > 0;
			float time = (((float)player.tickCount + partialTicks) / 200F) % 1F;
			VertexConsumer decayAlpha = buffersource.getBuffer(RenderType.dragonExplosionAlpha(DECAY_TEX));
			model.renderToBuffer(matrixStack, decayAlpha, bakedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, time);
			VertexConsumer decal = buffersource.getBuffer(RenderType.entityDecal(getTextureLocation(player)));
			model.renderToBuffer(matrixStack, decal, bakedLight, OverlayTexture.pack(0F, flag), 1F, 1F, 1F, 1F);
		matrixStack.popPose();
	}
	
	private void renderEnergy(PoseStack matrixStack, MultiBufferSource bufferSource, int bakedLight, T player, float partialTicks)
	{
		matrixStack.pushPose();
			float time = (float)player.tickCount + partialTicks;
			VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.energySwirl(ENERGY_TEX, xOffset(time) % 1F, time * 0.01F % 1F));
			this.getParentModel().copyPropertiesTo(this.glowModel);
			this.glowModel.renderToBuffer(matrixStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
		matrixStack.popPose();
	}
	
	private float xOffset(float time) { return Mth.cos(time * 0.02F) * 3F; }
	
	private void renderSkull(PoseStack matrixStack, MultiBufferSource bufferSource, float headYaw, float headPitch, int bakedLight)
	{
		matrixStack.pushPose();
			this.getParentModel().getHead().translateAndRotate(matrixStack);
			matrixStack.scale(-1F, 1F, -1F);
			this.skullModel.setupAnim(headPitch, headYaw, 0F);
			this.skullModel.renderToBuffer(matrixStack, bufferSource.getBuffer(RenderType.entityCutout(SKULL_TEX)), bakedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStack.popPose();
	}
}
