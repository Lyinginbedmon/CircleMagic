package com.lying.circles.client.renderer.entity;

import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.capabilities.PlayerData.EnumBodyPart;
import com.lying.circles.client.model.LichModel;
import com.lying.circles.client.model.LichSkullModel;
import com.lying.circles.client.renderer.CMModelLayers;
import com.lying.circles.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;

public class LichLayer<T extends Player, M extends HumanoidModel<T>> extends RenderLayer<T, M>
{
	private static final ResourceLocation ENERGY_TEX = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/lich.png");
	private static final ResourceLocation SKULL_TEX = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/lich_skull.png");
	private static final ResourceLocation DECAY_TEX = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/lich_decay.png");
	private static final RenderType DECAY_TYPE = RenderType.dragonExplosionAlpha(DECAY_TEX);
	private final LichModel<T> glowModel;
	private final LichSkullModel skullModel;
	
	// FIXME Individual limb decay rendering
	// FIXME Skin layer glowing in the dark
	
//	private static final Function<ResourceLocation, RenderType> DECAY_SHARD = Util.memoize((texture) -> {
//	      RenderType.CompositeState composite = 
//	    		  RenderType.CompositeState.builder()
//	    		  .setShaderState(RenderType.RENDERTYPE_ENTITY_ALPHA_SHADER)
//	    		  .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
//	    		  .setCullState(RenderType.NO_CULL).createCompositeState(true);
//	      return RenderType.create("entity_alpha", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, composite);
//	   });
//	private static final RenderType DECAY_TYPE = DECAY_SHARD.apply(DECAY_TEX);
	
	public LichLayer(RenderLayerParent<T, M> parent, EntityModelSet modelSet)
	{
		super(parent);
		this.glowModel = new LichModel<T>(modelSet.bakeLayer(CMModelLayers.LICH));
		this.skullModel = new LichSkullModel(modelSet.bakeLayer(CMModelLayers.LICH_SKULL));
	}
	
	public void render(PoseStack matrixStack, MultiBufferSource bufferSource, int packedLight, T player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(!PlayerData.isLich(player))
			return;
		
		renderSkull(matrixStack, bufferSource, 180F, limbSwing, packedLight);
		renderEnergy(matrixStack, bufferSource, ageInTicks, partialTicks);
		renderSkin(matrixStack, bufferSource, packedLight, player, partialTicks);
	}
	
	/** Renders the player's skin on top of the energy body */
	@SuppressWarnings("unchecked")
	private void renderSkin(PoseStack matrixStack, MultiBufferSource bufferSource, int packedLight, T player, float partialTicks)
	{
		// FIXME Fix transparent pixels in player skin displaying as opaque white
		PlayerData playerData = PlayerData.getCapability(player);
		System.out.println("Rendering limb decay");
		matrixStack.pushPose();
			PlayerModel<T> model = (PlayerModel<T>)getParentModel();
			boolean flag = player.hurtTime > 0;
			for(EnumBodyPart limb : EnumBodyPart.values())
			{
				float decayVolume = playerData == null ? 0F : playerData.getSkinDecay(limb);
				System.out.println("# Decay of "+limb.getSerializedName()+": "+(int)(decayVolume*100F));
				if(decayVolume < 1F)
					renderDecayedLimb(model, limb, decayVolume, flag, player, matrixStack, bufferSource, packedLight);
			}
		matrixStack.popPose();
	}
	
	private void renderDecayedLimb(PlayerModel<T> model, EnumBodyPart limb, float decayVolume, boolean hurt, T player, PoseStack matrixStack, MultiBufferSource bufferSource, int packedLight)
	{
		matrixStack.pushPose();
			if(decayVolume <= 0F)
			{
				prepareModel(model, limb, player);
				VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(getTextureLocation(player)));
				model.renderToBuffer(matrixStack, consumer, packedLight, OverlayTexture.pack(0F, hurt), 1F, 1F, 1F, 1F);
			}
			else
			{
				model.setAllVisible(true);
				VertexConsumer decayAlpha = bufferSource.getBuffer(DECAY_TYPE);
				model.renderToBuffer(matrixStack, decayAlpha, packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, decayVolume);
				
				prepareModel(model, limb, player);
				VertexConsumer decal = bufferSource.getBuffer(RenderType.entityDecal(getTextureLocation(player)));
				model.renderToBuffer(matrixStack, decal, packedLight, OverlayTexture.pack(0F, hurt), 1F, 1F, 1F, 1F);
			}
		matrixStack.popPose();
	}
	
	private void prepareModel(PlayerModel<T> model, EnumBodyPart limb, T player)
	{
		model.setAllVisible(false);
		switch(limb)
		{
			case HEAD:
				model.head.visible = true;
				model.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
				break;
			case LEFT_ARM:
				model.leftArm.visible = true;
				model.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
				break;
			case LEFT_LEG:
				model.leftLeg.visible = true;
				model.leftPants.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
				break;
			case RIGHT_ARM:
				model.rightArm.visible = true;
				model.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
				break;
			case RIGHT_LEG:
				model.rightLeg.visible = true;
				model.rightPants.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
				break;
			case TORSO:
				model.body.visible = true;
				model.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
				break;
		}
	}
	
	/** Renders a body of swirling energy */
	private void renderEnergy(PoseStack matrixStack, MultiBufferSource bufferSource, float ageInTicks, float partialTicks)
	{
		matrixStack.pushPose();
			float time = ageInTicks + partialTicks;
			VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.energySwirl(ENERGY_TEX, xOffset(time) % 1F, time * 0.01F % 1F));
			this.getParentModel().copyPropertiesTo(this.glowModel);
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
