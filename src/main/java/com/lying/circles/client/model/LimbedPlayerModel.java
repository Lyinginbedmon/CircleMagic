package com.lying.circles.client.model;

import com.lying.circles.capabilities.PlayerData.EnumBodyPart;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class LimbedPlayerModel<T extends LivingEntity> extends PlayerModel<T>
{
	private final EnumBodyPart limb;
	
	public LimbedPlayerModel(ModelPart parts, boolean slim, EnumBodyPart limbIn)
	{
		super(parts, slim);
		this.limb = limbIn;
	}
	
	public void copyPropertiesFrom(HumanoidModel<T> model)
	{
		model.copyPropertiesTo(this);
		hideOtherLimbs();
	}
	
	public void hideOtherLimbs()
	{
		switch(limb)
		{
			case HEAD:
				this.body.visible = this.jacket.visible = false;
				this.leftLeg.visible = this.leftPants.visible = false;
				this.rightLeg.visible = this.rightPants.visible = false;
				this.leftArm.visible = this.leftSleeve.visible = false;
				this.rightArm.visible = this.rightSleeve.visible = false;
				break;
			case LEFT_ARM:
				this.head.visible = this.hat.visible = false;
				this.body.visible = this.jacket.visible = false;
				this.leftLeg.visible = this.leftPants.visible = false;
				this.rightLeg.visible = this.rightPants.visible = false;
				this.rightArm.visible = this.rightSleeve.visible = false;
				break;
			case LEFT_LEG:
				this.head.visible = this.hat.visible = false;
				this.body.visible = this.jacket.visible = false;
				this.rightLeg.visible = this.rightPants.visible = false;
				this.leftArm.visible = this.leftSleeve.visible = false;
				this.rightArm.visible = this.rightSleeve.visible = false;
				break;
			case RIGHT_ARM:
				this.head.visible = this.hat.visible = false;
				this.body.visible = this.jacket.visible = false;
				this.leftLeg.visible = this.leftPants.visible = false;
				this.rightLeg.visible = this.rightPants.visible = false;
				this.leftArm.visible = this.leftSleeve.visible = false;
				break;
			case RIGHT_LEG:
				this.head.visible = this.hat.visible = false;
				this.body.visible = this.jacket.visible = false;
				this.leftLeg.visible = this.leftPants.visible = false;
				this.leftArm.visible = this.leftSleeve.visible = false;
				this.rightArm.visible = this.rightSleeve.visible = false;
				break;
			case TORSO:
				this.head.visible = this.hat.visible = false;
				this.leftLeg.visible = this.leftPants.visible = false;
				this.rightLeg.visible = this.rightPants.visible = false;
				this.leftArm.visible = this.leftSleeve.visible = false;
				this.rightArm.visible = this.rightSleeve.visible = false;
				break;
		}
	}
}
