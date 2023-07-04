package com.lying.circles;

import org.slf4j.Logger;

import com.lying.circles.client.ClientSetupEvents;
import com.lying.circles.client.particle.M19Particles;
import com.lying.circles.client.renderer.magic.ComponentEffectsRegistry;
import com.lying.circles.data.CMDataGenerators;
import com.lying.circles.init.CMBlockEntities;
import com.lying.circles.init.CMBlocks;
import com.lying.circles.init.CMCapabilities;
import com.lying.circles.init.CMEntities;
import com.lying.circles.init.CMItems;
import com.lying.circles.init.CMMenus;
import com.lying.circles.init.FunctionRecipes;
import com.lying.circles.init.SpellComponents;
import com.lying.circles.init.SpellVariables;
import com.lying.circles.network.PacketHandler;
import com.lying.circles.reference.Reference;
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
public class CircleMagic
{
    // Directly reference a slf4j logger
    public static final Logger LOG = LogUtils.getLogger();
    
    public static final IEventBus EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();
    
    public CircleMagic()
    {
    	DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
		{
			ComponentEffectsRegistry.EFFECTS.register(EVENT_BUS);
			M19Particles.PARTICLES.register(EVENT_BUS);
			EVENT_BUS.register(ClientSetupEvents.class);
		});
    	
        EVENT_BUS.addListener(this::commonSetup);
        EVENT_BUS.addListener(CMDataGenerators::onGatherData);
        EVENT_BUS.addListener(CMCapabilities::onRegisterCapabilities);
        
        CMEntities.ENTITIES.register(EVENT_BUS);
        CMBlocks.BLOCKS.register(EVENT_BUS);
        CMBlockEntities.BLOCK_ENTITIES.register(EVENT_BUS);
        CMItems.ITEMS.register(EVENT_BUS);
        CMMenus.MENUS.register(EVENT_BUS);
        
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
    		
    		((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(CMBlocks.MAGIC_SAPLING.getId(), () -> CMBlocks.POTTED_MAGIC_SAPLING.get());
    		
            CMItems.addBrewingRecipes();
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
