package com.lying.circles.client.model;

import java.util.Map;

import com.lying.circles.capabilities.PlayerData.EnumBodyPart;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public class CurruisisModel<T extends LivingEntity> extends HumanoidModel<T>
{
	public CurruisisModel(ModelPart p_170677_)
	{
		super(p_170677_);
	}
	
	// TODO Improve curruisis modelling
	public static MeshDefinition createMesh(CubeDeformation deformation, float scale)
	{
		MeshDefinition meshdefinition = HumanoidModel.createMesh(deformation, scale);
		PartDefinition partdefinition = meshdefinition.getRoot();
		
		return meshdefinition;
	}
	
	public void adjustCurruisis(Map<EnumBodyPart, Integer> curruisisMap, int stage)
	{
		setAllVisible(false);
		curruisisMap.entrySet().forEach((entry) -> 
		{
			ModelPart limb = null;
			switch(entry.getKey())
			{
				case HEAD:
					limb = this.head;
					break;
				case LEFT_ARM:
					limb = this.leftArm;
					break;
				case LEFT_LEG:
					limb = this.leftLeg;
					break;
				case RIGHT_ARM:
					limb = this.rightArm;
					break;
				case RIGHT_LEG:
					limb = this.rightLeg;
					break;
				case TORSO:
					limb = this.body;
					break;
			}
			
			if(limb != null)
				limb.visible = entry.getValue() == stage;
		});
	}
}
