package com.lying.misc19.client.renderer.entity;

import java.util.List;

import com.lying.misc19.client.renderer.ComponentRenderers;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.utility.SpellData;
import com.lying.misc19.utility.SpellManager;
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
	private static final Vec3 OFFSET = new Vec3(0D, 0D, 0.1D);
	
	public SpellLayer(RenderLayerParent<T, M> parentRenderer)
	{
		super(parentRenderer);
	}
	
	public void render(PoseStack matrixStack, MultiBufferSource bufferSource, int p_117351_, T livingEntity, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_)
	{
		SpellManager manager = SpellManager.instance(livingEntity.getLevel());
		List<SpellData> spells = manager.getSpellsOn(livingEntity);
		if(spells.isEmpty())
			return;
		
		matrixStack.pushPose();
			matrixStack.translate(0D, 1.501F, 0D);
			matrixStack.translate(0D, -livingEntity.getBbHeight() * 0.5D, 0D);
			matrixStack.pushPose();
				matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90F));
				for(int i=0; i<spells.size(); i++)
				{
					matrixStack.pushPose();
						int pair = (int)Math.ceil(i * 0.5D);
						int sign = i%2 == 0 ? 1 : -1;
						Vec3 position = OFFSET.scale(pair * sign);
						matrixStack.translate(position.x, position.y, position.z);
						matrixStack.pushPose();
							matrixStack.scale(2.2F, 2.2F, 2.2F);
							ISpellComponent arrangement = spells.get(i).arrangement();
							ComponentRenderers.renderWorld(arrangement, matrixStack, bufferSource);
						matrixStack.popPose();
					matrixStack.popPose();
				}
			matrixStack.popPose();
		matrixStack.popPose();
	}
}
