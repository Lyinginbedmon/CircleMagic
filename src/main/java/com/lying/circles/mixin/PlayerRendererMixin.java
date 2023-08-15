package com.lying.circles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.circles.capabilities.PlayerData;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin
{
	@Inject(method = "setModelProperties(Lnet/minecraft/client/player/AbstractClientPlayer;)V", at = @At("RETURN"), cancellable = true)
	public void setModelProperties(AbstractClientPlayer player, final CallbackInfo ci)
	{
		if(!PlayerData.isLich(player))
			return;
		
		PlayerRenderer renderer = (PlayerRenderer)(Object)this;
		PlayerModel<AbstractClientPlayer> model = renderer.getModel();
		ModelPart[][] parts = new ModelPart[][] { 
			new ModelPart[] { model.hat, model.head },
			new ModelPart[] { model.body },
			new ModelPart[] { model.leftArm },
			new ModelPart[] { model.rightArm },
			new ModelPart[] { model.leftLeg },
			new ModelPart[] { model.rightLeg }};
		
		for(ModelPart[] set : parts)
			for(ModelPart part : set)
				part.visible = false;
	}
}
