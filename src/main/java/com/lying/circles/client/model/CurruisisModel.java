package com.lying.circles.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public class CurruisisModel<T extends LivingEntity> extends HumanoidModel<T>
{
	public CurruisisModel(ModelPart p_170677_)
	{
		super(p_170677_);
	}
	
	public static MeshDefinition createMesh(CubeDeformation deformation, float scale)
	{
		MeshDefinition meshdefinition = HumanoidModel.createMesh(deformation, scale);
		PartDefinition partdefinition = meshdefinition.getRoot();
		
		PartDefinition right_arm = partdefinition.getChild("right_arm");
			right_arm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(6, 10).addBox(-8.5F, -4.75F, 0.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(6, 10).addBox(-6.25F, -5.75F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -22.0F, 0.0F, -0.5236F, 0.0F, 0.0F));
			right_arm.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 13).addBox(-8.75F, -0.75F, -2.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, 0.7854F, 0.0F, 0.0F));
			right_arm.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, 0.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -11.0F, -2.0F, 0.0F, 0.0F, 0.7854F));
		
		PartDefinition left_arm = partdefinition.getChild("left_arm");
			left_arm.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(6, 10).addBox(9.5F, -3.75F, 1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.3876F, 0.3614F, 0.7137F));
			left_arm.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 10).addBox(11.0F, -6.0F, 4.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -11.0F, -2.0F, 0.0F, 0.0F, 0.7854F));
		
		return meshdefinition;
	}
}
