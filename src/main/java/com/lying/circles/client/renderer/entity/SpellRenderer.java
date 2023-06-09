package com.lying.circles.client.renderer.entity;

import com.lying.circles.entities.SpellEntity;
import com.lying.circles.utility.SpellData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class SpellRenderer extends EntityRenderer<SpellEntity>
{
	public SpellRenderer(Context contextIn)
	{
		super(contextIn);
	}
	
	public ResourceLocation getTextureLocation(SpellEntity entity) { return null; }
	
	public void render(SpellEntity spellEntity, float p_115037_, float p_115038_, PoseStack matrixStack, MultiBufferSource bufferSource, int p_115041_)
	{
		SpellData spell = spellEntity.getSpell();
		if(spell == null)
			return;
		
		RenderSystem.setShaderColor(1F, 1F, 1F, spellEntity.getVisibility());
		matrixStack.pushPose();
			matrixStack.translate(0D, spellEntity.getBbHeight() * 0.5D, 0D);
			matrixStack.mulPose(Vector3f.YP.rotationDegrees(-spellEntity.getYRot()));
			matrixStack.mulPose(Vector3f.XP.rotationDegrees(spellEntity.getXRot()));
			matrixStack.pushPose();
				spellEntity.getCanvas().drawIntoWorld(matrixStack, bufferSource);
			matrixStack.popPose();
		matrixStack.popPose();
	}
}
