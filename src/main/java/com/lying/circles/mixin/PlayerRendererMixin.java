package com.lying.circles.mixin;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.base.Function;
import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.capabilities.PlayerData.EnumBodyPart;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin
{
	private static final Map<EnumBodyPart, Function<PlayerModel<AbstractClientPlayer>, ModelPart[]>> LIMB_MAP = new HashMap<>();
	
	@Inject(method = "setModelProperties(Lnet/minecraft/client/player/AbstractClientPlayer;)V", at = @At("RETURN"), cancellable = true)
	public void setModelProperties(AbstractClientPlayer player, final CallbackInfo ci)
	{
		if(!PlayerData.isLich(player) || !PlayerData.getCapability(player).hasSkinDecay())
			return;
		
		PlayerData data = PlayerData.getCapability(player);
		PlayerRenderer renderer = (PlayerRenderer)(Object)this;
		PlayerModel<AbstractClientPlayer> model = renderer.getModel();
		
		// Hide any parts that have decay
		for(EnumBodyPart limb : EnumBodyPart.values())
		{
			float decay = data.getSkinDecay(limb);
			for(ModelPart part : LIMB_MAP.get(limb).apply(model))
				if(part.visible && decay > 0F)
					part.visible = false;
		}
	}
	
	static
	{
		LIMB_MAP.put(EnumBodyPart.HEAD, (model) -> { return new ModelPart[] {model.hat, model.head}; });
		LIMB_MAP.put(EnumBodyPart.TORSO, (model) -> { return new ModelPart[] {model.body, model.jacket}; });
		LIMB_MAP.put(EnumBodyPart.LEFT_ARM, (model) -> { return new ModelPart[] {model.leftArm, model.leftSleeve}; });
		LIMB_MAP.put(EnumBodyPart.LEFT_LEG, (model) -> { return new ModelPart[] {model.leftLeg, model.leftPants}; });
		LIMB_MAP.put(EnumBodyPart.RIGHT_ARM, (model) -> { return new ModelPart[] {model.rightArm, model.rightSleeve}; });
		LIMB_MAP.put(EnumBodyPart.RIGHT_LEG, (model) -> { return new ModelPart[] {model.rightLeg, model.rightPants}; });
	}
}
