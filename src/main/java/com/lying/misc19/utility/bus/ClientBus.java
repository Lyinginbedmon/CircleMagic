package com.lying.misc19.utility.bus;

import com.lying.misc19.client.renderer.magic.ComponentEffectsRegistry;

import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientBus
{
	@SubscribeEvent
	public static void onRenderLevel(final RenderLevelStageEvent event)
	{
		if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY)
			ComponentEffectsRegistry.tickActiveEffects(event);
	}
	
	@SubscribeEvent
	public static void onChangeDimension(final PlayerEvent.PlayerChangedDimensionEvent event)
	{
		ComponentEffectsRegistry.clear();
	}
}
