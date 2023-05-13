package com.lying.misc19.client.renderer;

import com.lying.misc19.client.renderer.blockentity.PhantomBlockEntityRenderer;
import com.lying.misc19.client.renderer.entity.SpellRenderer;
import com.lying.misc19.init.M19BlockEntities;
import com.lying.misc19.init.M19Entities;
import com.lying.misc19.reference.Reference;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RenderRegistry
{
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerEntityRenderer(M19Entities.SPELL.get(), SpellRenderer::new);
	}
	
	@SubscribeEvent
	public static void registerTileRenderers(ModelEvent.RegisterAdditional event)
	{
		BlockEntityRenderers.register(M19BlockEntities.PHANTOM_CUBE.get(), PhantomBlockEntityRenderer::new);
	}
}
