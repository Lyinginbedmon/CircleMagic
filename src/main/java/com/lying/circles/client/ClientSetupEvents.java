package com.lying.circles.client;

import com.lying.circles.CircleMagic;
import com.lying.circles.capabilities.LivingData;
import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.client.gui.screen.ScreenSandbox;
import com.lying.circles.client.model.CurruisisModel;
import com.lying.circles.client.model.LichModel;
import com.lying.circles.client.model.LichSkullModel;
import com.lying.circles.client.renderer.CMModelLayers;
import com.lying.circles.client.renderer.entity.CurruisisLayer;
import com.lying.circles.client.renderer.entity.LichLayer;
import com.lying.circles.client.renderer.entity.PendulumLayer;
import com.lying.circles.client.renderer.entity.SpellLayer;
import com.lying.circles.init.CMBlocks;
import com.lying.circles.init.CMItems;
import com.lying.circles.init.CMMenus;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.SpellManager;
import com.lying.circles.utility.bus.ClientBus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetupEvents
{
	private static final Minecraft MC = Minecraft.getInstance();
	private static SpellManager LOCAL_SPELL_MANAGER = null;
	public static PlayerData LOCAL_DATA_PLAYER = new PlayerData(null);
	public static LivingData LOCAL_DATA_LIVING = new LivingData(null);
	
    @SuppressWarnings("removal")
	@SubscribeEvent
	public static void clientInit(final FMLClientSetupEvent event)
	{
		event.enqueueWork(() ->
		{
			CircleMagic.EVENT_BUS.register(new ClientBus());
			
			ItemBlockRenderTypes.setRenderLayer(CMBlocks.PHANTOM_CUBE.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(CMBlocks.FAIRY_JAR.get(), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(CMBlocks.POTTED_MAGIC_SAPLING.get(), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(CMBlocks.MAGIC_SAPLING.get(), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(CMBlocks.MAGIC_TREE.get(), RenderType.cutout());
			
        	MinecraftForge.EVENT_BUS.register(ClientBus.class);
			
			ItemProperties.register(CMItems.PENDULUM.get(), new ResourceLocation(Reference.ModInfo.MOD_ID, "cast"), (stack, world, entity, slot) ->
			{
				return entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1F : 0F;
			});
			
			MenuScreens.register(CMMenus.SANDBOX_MENU.get(), ScreenSandbox::new);
		});
	}
    
    @SubscribeEvent
    public static void registerOverlayEvent(final RegisterGuiOverlaysEvent event)
    {
    	event.registerAboveAll("mana_bar", new OverlayManaBar());
    }
    
    @SubscribeEvent
    public static void registerColorHandlersBlock(final RegisterColorHandlersEvent.Block event)
    {
    	event.register((blockState, tintGetter, pos, layer) -> { return tintGetter != null && pos != null ? BiomeColors.getAverageWaterColor(tintGetter, pos) : -1; }, CMBlocks.CRUCIBLE.get());
    	event.register((blockState, tintGetter, pos, layer) -> { return 3650559; }, CMBlocks.MAGIC_TREE.get());
    }
    
    @SubscribeEvent
    public static void registerLayersEvent(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
    	event.registerLayerDefinition(CMModelLayers.STATUE, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0F), 64, 32));
    	event.registerLayerDefinition(CMModelLayers.CURRUISIS, () -> LayerDefinition.create(CurruisisModel.createMesh(CubeDeformation.NONE, 0F), 32, 32));
    	event.registerLayerDefinition(CMModelLayers.LICH, () -> LayerDefinition.create(LichModel.createMesh(CubeDeformation.NONE, 0F), 64, 32));
    	event.registerLayerDefinition(CMModelLayers.LICH_SKULL, () -> LichSkullModel.createSkullModel());
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@SubscribeEvent
    public static void addEntityRendererLayers(EntityRenderersEvent.AddLayers event)
    {
    	EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
    	for(EntityRenderer<? extends Player> renderer : dispatcher.getSkinMap().values())
    	{
    		PlayerRenderer value = (PlayerRenderer)renderer;
    		value.addLayer((PendulumLayer)(new PendulumLayer<>(value)));
    		value.addLayer((SpellLayer)(new SpellLayer<>(value)));
    		value.addLayer((CurruisisLayer)(new CurruisisLayer(value, event.getEntityModels())));
    		value.addLayer((LichLayer)(new LichLayer(value, event.getEntityModels())));
    	}
    	dispatcher.renderers.forEach((type,renderer) -> 
    	{
    		if(LivingEntityRenderer.class.isAssignableFrom(renderer.getClass()))
    		{
    			LivingEntityRenderer livingRender = (LivingEntityRenderer)renderer;
    			livingRender.addLayer((SpellLayer)(new SpellLayer<>(livingRender)));
    		}
    	});
    }
	
	public static SpellManager getLocalData()
	{
		if(LOCAL_SPELL_MANAGER == null)
			LOCAL_SPELL_MANAGER = new ClientSpellManager(MC.player.getLevel());
		return LOCAL_SPELL_MANAGER;
	}
	
	public static ClientLevel getLevel() { return MC.level; }
	
	public static Player getLocalPlayer() { return MC.player; }
	
	public static PlayerData getPlayerData(Player playerIn)
	{
		LOCAL_DATA_PLAYER.setPlayer(playerIn);
		return LOCAL_DATA_PLAYER;
	}
	
	public static LivingData getLivingData(LivingEntity playerIn)
	{
		LOCAL_DATA_LIVING.setLiving(playerIn);
		return LOCAL_DATA_LIVING;
	}
}
