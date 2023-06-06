package com.lying.misc19;

import org.slf4j.Logger;

import com.lying.misc19.client.ClientSetupEvents;
import com.lying.misc19.client.particle.M19Particles;
import com.lying.misc19.client.renderer.magic.ComponentEffectsRegistry;
import com.lying.misc19.data.M19DataGenerators;
import com.lying.misc19.init.FunctionRecipes;
import com.lying.misc19.init.M19BlockEntities;
import com.lying.misc19.init.M19Blocks;
import com.lying.misc19.init.M19Entities;
import com.lying.misc19.init.M19Items;
import com.lying.misc19.init.M19Menus;
import com.lying.misc19.init.SpellComponents;
import com.lying.misc19.init.SpellVariables;
import com.lying.misc19.network.PacketHandler;
import com.lying.misc19.reference.Reference;
import com.mojang.logging.LogUtils;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Reference.ModInfo.MOD_ID)
public class Misc19
{
    // Directly reference a slf4j logger
    public static final Logger LOG = LogUtils.getLogger();
    
    public static final IEventBus EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();
    
    public Misc19()
    {
    	DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
		{
			ComponentEffectsRegistry.EFFECTS.register(EVENT_BUS);
			M19Particles.PARTICLES.register(EVENT_BUS);
			EVENT_BUS.register(ClientSetupEvents.class);
		});
    	
        EVENT_BUS.addListener(this::commonSetup);
        EVENT_BUS.addListener(M19DataGenerators::onGatherData);
        
        M19Entities.ENTITIES.register(EVENT_BUS);
        M19Blocks.BLOCKS.register(EVENT_BUS);
        M19BlockEntities.BLOCK_ENTITIES.register(EVENT_BUS);
        M19Items.ITEMS.register(EVENT_BUS);
        M19Menus.MENUS.register(EVENT_BUS);
        
        FunctionRecipes.REGISTRY.register(EVENT_BUS);
        SpellComponents.COMPONENTS.register(EVENT_BUS);
        SpellVariables.VARIABLES.register(EVENT_BUS);
        EVENT_BUS.addListener(SpellComponents::reportInit);
        
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event)
    {
    	event.enqueueWork(() -> 
    	{
    		PacketHandler.init();
    		
    		((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(M19Blocks.MAGIC_SAPLING.getId(), () -> M19Blocks.POTTED_MAGIC_SAPLING.get());
    	});
    }
    
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    	
    }
    
    @SubscribeEvent
    public void onReloadListenersEvent(AddReloadListenerEvent event)
    {
    	event.addListener(FunctionRecipes.getInstance());
    }
}
