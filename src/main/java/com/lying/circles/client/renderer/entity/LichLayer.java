package com.lying.circles.client.renderer.entity;

import java.util.HashMap;
import java.util.Map;

import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.capabilities.PlayerData.EnumBodyPart;
import com.lying.circles.client.model.LichModel;
import com.lying.circles.client.model.LichSkullModel;
import com.lying.circles.client.model.LimbedPlayerModel;
import com.lying.circles.client.renderer.CMModelLayers;
import com.lying.circles.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
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
	private static final ResourceLocation LICH_TEX = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/lich.png");
	private static final ResourceLocation ENERGY_TEX = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/lich_energy.png");
	private static final ResourceLocation SKULL_TEX = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/lich_skull.png");
	private static final ResourceLocation DECAY_TEX = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/lich_decay.png");
	private static final RenderType DECAY_TYPE = RenderType.dragonExplosionAlpha(DECAY_TEX);
	private final LichModel<T> glowModel;
	private final LichSkullModel skullModel;
	
	// FIXME Fix transparent pixels in player skin displaying as opaque white
	
	private final Map<Boolean, Map<EnumBodyPart, LimbedPlayerModel<T>>> limbMap = new HashMap<>();
	
	public LichLayer(RenderLayerParent<T, M> parent, EntityModelSet modelSet)
	{
		super(parent);
		this.glowModel = new LichModel<T>(modelSet.bakeLayer(CMModelLayers.LICH));
		this.skullModel = new LichSkullModel(modelSet.bakeLayer(CMModelLayers.LICH_SKULL));
		
		Map<EnumBodyPart, LimbedPlayerModel<T>> slimLimbs = new HashMap<>();
		Map<EnumBodyPart, LimbedPlayerModel<T>> normLimbs = new HashMap<>();
		for(EnumBodyPart limb : EnumBodyPart.values())
		{
			slimLimbs.put(limb, new LimbedPlayerModel<T>(modelSet.bakeLayer(ModelLayers.PLAYER_SLIM), true, limb));
			normLimbs.put(limb, new LimbedPlayerModel<T>(modelSet.bakeLayer(ModelLayers.PLAYER), false, limb));
		}
		limbMap.put(true, slimLimbs);
		limbMap.put(false, normLimbs);
	}
	
	public void render(PoseStack matrixStack, MultiBufferSource bufferSource, int packedLight, T player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(!PlayerData.isLich(player) || !PlayerData.getCapability(player).hasSkinDecay())
			return;
		
		renderSkull(matrixStack, bufferSource, 180F, limbSwing, packedLight);
		renderEnergy(matrixStack, bufferSource, player, ageInTicks, partialTicks);
		renderSkin(matrixStack, bufferSource, packedLight, player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
	}
	
	/** Renders the player's skin on top of the energy body */
	private void renderSkin(PoseStack matrixStack, MultiBufferSource bufferSource, int packedLight, T player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float partialTicks)
	{
		PlayerData playerData = PlayerData.getCapability(player);
		matrixStack.pushPose();
			boolean flag = player.hurtTime > 0;
			for(EnumBodyPart limb : EnumBodyPart.values())
			{
				float decayVolume = playerData == null ? 0F : playerData.getSkinDecay(limb);
				if(decayVolume < 1F && decayVolume > 0F)
					renderDecayedLimb(limb, decayVolume, flag, player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, matrixStack, bufferSource, packedLight);
			}
		matrixStack.popPose();
	}
	
	private void renderDecayedLimb(EnumBodyPart limb, float decayVolume, boolean hurt, T player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int packedLight)
	{
		matrixStack.pushPose();
			LimbedPlayerModel<T> model = limbMap.get(((AbstractClientPlayer)player).getModelName().equalsIgnoreCase("slim")).get(limb);
			model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks + partialTicks, netHeadYaw, headPitch);
			getParentModel().copyPropertiesTo(model);
			model.hideOtherLimbs();
			
			VertexConsumer decayAlpha = bufferSource.getBuffer(DECAY_TYPE);
			model.renderToBuffer(matrixStack, decayAlpha, packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, decayVolume);
			
			VertexConsumer decal = bufferSource.getBuffer(RenderType.entityDecal(LICH_TEX));
			model.renderToBuffer(matrixStack, decal, packedLight, OverlayTexture.pack(0F, hurt), 1F, 1F, 1F, 1F);
		matrixStack.popPose();
	}
	
	/** Renders a body of swirling energy */
	private void renderEnergy(PoseStack matrixStack, MultiBufferSource bufferSource, T player, float ageInTicks, float partialTicks)
	{
		matrixStack.pushPose();
			getParentModel().copyPropertiesTo(this.glowModel);
			float time = ageInTicks + partialTicks;
			VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.energySwirl(ENERGY_TEX, xOffset(time) % 1F, time * 0.01F % 1F));
			this.glowModel.prepareModel(PlayerData.getCapability(player));
			this.glowModel.renderToBuffer(matrixStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
		matrixStack.popPose();
	}
	
	private float xOffset(float time) { return Mth.cos(time * 0.02F) * 3F; }
	
	/** Renders a disembodied skull inside the player's head */
	private void renderSkull(PoseStack matrixStack, MultiBufferSource bufferSource, float headYaw, float headPitch, int packedLight)
	{
		matrixStack.pushPose();
			this.getParentModel().getHead().translateAndRotate(matrixStack);
			matrixStack.scale(-1F, 1F, -1F);
			this.skullModel.setupAnim(headPitch, headYaw, 0F);
			this.skullModel.renderToBuffer(matrixStack, bufferSource.getBuffer(RenderType.entityCutout(SKULL_TEX)), packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStack.popPose();
	}
}
