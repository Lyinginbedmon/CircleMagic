package com.lying.circles.client.model;

import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.capabilities.PlayerData.EnumBodyPart;

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
		float rescale = 0.01F;
		CubeDeformation partScale = new CubeDeformation(-rescale);
		parts.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, partScale), PartPose.offset(0.0F, 0.0F + scale, 0.0F));
		parts.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F + scale, 0.0F));
		parts.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, partScale), PartPose.offset(0.0F, 0.0F + scale, 0.0F));
		parts.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, partScale), PartPose.offset(-5.0F, 2.0F + scale, 0.0F));
		parts.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, partScale), PartPose.offset(5.0F, 2.0F + scale, 0.0F));
		parts.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, partScale), PartPose.offset(-1.9F, 12.0F + scale, 0.0F));
		parts.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, partScale), PartPose.offset(1.9F, 12.0F + scale, 0.0F));
		return mesh;
	}
	
	public void prepareModel(PlayerData data)
	{
		this.setAllVisible(false);
		for(EnumBodyPart limb : EnumBodyPart.values())
		{
			if(data.getSkinDecay(limb) == 0F)
				continue;
			
			switch(limb)
			{
				case HEAD:
					this.head.visible = this.hat.visible = true;
					break;
				case LEFT_ARM:
					this.leftArm.visible = true;
					break;
				case LEFT_LEG:
					this.leftLeg.visible = true;
					break;
				case RIGHT_ARM:
					this.rightArm.visible = true;
					break;
				case RIGHT_LEG:
					this.rightLeg.visible = true;
					break;
				case TORSO:
					this.body.visible = true;
					break;
			}
		}
	}
}
