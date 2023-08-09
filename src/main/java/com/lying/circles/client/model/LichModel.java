package com.lying.circles.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public class LichModel<T extends LivingEntity> extends HumanoidModel<T>
{
	public LichModel(ModelPart modelPart)
	{
		super(modelPart);
	}
	
	public static MeshDefinition createMesh(CubeDeformation deformation, float scale)
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition parts = mesh.getRoot();
		parts.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, deformation.extend(-0.5F)), PartPose.offset(0.0F, 0.0F + scale, 0.0F));
		parts.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F + scale, 0.0F));
		parts.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, deformation.extend(-0.5F)), PartPose.offset(0.0F, 0.0F + scale, 0.0F));
		parts.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(-0.5F)), PartPose.offset(-5.0F, 2.0F + scale, 0.0F));
		parts.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(-0.5F)), PartPose.offset(5.0F, 2.0F + scale, 0.0F));
		parts.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(-0.5F)), PartPose.offset(-1.9F, 12.0F + scale, 0.0F));
		parts.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(-0.5F)), PartPose.offset(1.9F, 12.0F + scale, 0.0F));
		return mesh;
	}
}
