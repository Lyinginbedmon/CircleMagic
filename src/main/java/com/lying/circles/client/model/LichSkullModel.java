package com.lying.circles.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class LichSkullModel extends SkullModelBase
{
	private final ModelPart root;
	protected final ModelPart head;
	
	public LichSkullModel(ModelPart partsIn)
	{
		this.root = partsIn;
		this.head = partsIn.getChild("head");
	}
	
	public static LayerDefinition createSkullModel()
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		part.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8, 8, 8, new CubeDeformation(-0.5F)), PartPose.ZERO);
		
		return LayerDefinition.create(mesh, 64, 32);
	}
	
	public void setupAnim(float p_170950_, float headYaw, float headPitch)
	{
		this.head.yRot = headYaw * ((float)Math.PI / 180F);
		this.head.xRot = headPitch * ((float)Math.PI / 180F);
	}
	
	public void renderToBuffer(PoseStack p_103815_, VertexConsumer p_103816_, int p_103817_, int p_103818_, float p_103819_, float p_103820_, float p_103821_, float p_103822_)
	{
		this.root.render(p_103815_, p_103816_, p_103817_, p_103818_, p_103819_, p_103820_, p_103821_, p_103822_);
	}

}
