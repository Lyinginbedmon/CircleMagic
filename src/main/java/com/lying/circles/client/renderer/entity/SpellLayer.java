package com.lying.circles.client.renderer.entity;

import java.util.List;

import com.lying.circles.client.Canvas;
import com.lying.circles.client.ClientSetupEvents;
import com.lying.circles.client.ClientSpellManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class SpellLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M>
{
	private static final Vec3 OFFSET = new Vec3(0D, 0D, 0.2D);
	
	public SpellLayer(RenderLayerParent<T, M> parentRenderer)
	{
		super(parentRenderer);
	}
	
	public void render(PoseStack matrixStack, MultiBufferSource bufferSource, int p_117351_, T livingEntity, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_)
	{
		ClientSpellManager manager = (ClientSpellManager)ClientSetupEvents.getLocalData();
		List<Canvas> spells = manager.getSpellCanvasOn(livingEntity);
		if(spells.isEmpty())
			return;
		
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		matrixStack.pushPose();
			matrixStack.translate(0D, livingEntity.getBbHeight() * 0.5D, 0D);
			matrixStack.pushPose();
				matrixStack.mulPose(Vector3f.YP.rotationDegrees(-livingEntity.getYRot()));
				matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90F));
				for(int i=0; i<spells.size(); i++)
				{
					matrixStack.pushPose();
						int pair = (int)Math.ceil(i * 0.5D);
						int sign = i%2 == 0 ? 1 : -1;
						Vec3 position = OFFSET.scale(pair * sign);
						matrixStack.translate(position.x, position.y, position.z);
//						matrixStack.scale(2.2F, 2.2F, 2.2F);
						spells.get(i).drawIntoWorld(matrixStack, bufferSource);
					matrixStack.popPose();
				}
			matrixStack.popPose();
		matrixStack.popPose();
	}
}
