package com.lying.misc19.utility.bus;

import com.lying.misc19.reference.Reference;
import com.lying.misc19.utility.SpellManager;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerBus
{
	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent event)
	{
		if(event.level.isClientSide() || event.phase == TickEvent.Phase.END)
			return;
		
		SpellManager manager = SpellManager.instance(event.level);
		if(!manager.isEmpty())
			manager.tick();
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingDeath(LivingDeathEvent event)
	{
		if(event.isCanceled())
			return;
		
		SpellManager.instance(event.getEntity().getLevel()).removeSpellsFrom(event.getEntity());
	}
}
