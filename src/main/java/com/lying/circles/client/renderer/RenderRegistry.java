package com.lying.circles.client.renderer;

import com.lying.circles.client.renderer.blockentity.CrucibleBlockEntityRenderer;
import com.lying.circles.client.renderer.blockentity.FairyJarBlockEntityRenderer;
import com.lying.circles.client.renderer.blockentity.InscribedBlockEntityRenderer;
import com.lying.circles.client.renderer.blockentity.MagicTreeBlockEntityRenderer;
import com.lying.circles.client.renderer.blockentity.PhantomBlockEntityRenderer;
import com.lying.circles.client.renderer.blockentity.StatueBlockEntityRenderer;
import com.lying.circles.client.renderer.entity.SpellRenderer;
import com.lying.circles.client.renderer.entity.StatueRenderer;
import com.lying.circles.init.CMBlockEntities;
import com.lying.circles.init.CMEntities;
import com.lying.circles.reference.Reference;

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
		event.registerEntityRenderer(CMEntities.SPELL.get(), SpellRenderer::new);
		event.registerEntityRenderer(CMEntities.STATUE.get(), StatueRenderer::new);
	}
	
	@SubscribeEvent
	public static void registerTileRenderers(ModelEvent.RegisterAdditional event)
	{
		BlockEntityRenderers.register(CMBlockEntities.PHANTOM_CUBE.get(), PhantomBlockEntityRenderer::new);
		BlockEntityRenderers.register(CMBlockEntities.CRUCIBLE.get(), CrucibleBlockEntityRenderer::new);
		BlockEntityRenderers.register(CMBlockEntities.FAIRY_JAR.get(), FairyJarBlockEntityRenderer::new);
		BlockEntityRenderers.register(CMBlockEntities.INSCRIPTION.get(), InscribedBlockEntityRenderer::new);
		BlockEntityRenderers.register(CMBlockEntities.MAGIC_TREE.get(), MagicTreeBlockEntityRenderer::new);
		BlockEntityRenderers.register(CMBlockEntities.STATUE.get(), StatueBlockEntityRenderer::new);
	}
}
