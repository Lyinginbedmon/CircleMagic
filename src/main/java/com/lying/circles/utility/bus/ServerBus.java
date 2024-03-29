package com.lying.circles.utility.bus;

import com.lying.circles.blocks.ICruciblePart;
import com.lying.circles.blocks.TilledSand;
import com.lying.circles.blocks.TilledSand.Shape;
import com.lying.circles.capabilities.LivingData;
import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.init.CMBlocks;
import com.lying.circles.init.CMDamageSource;
import com.lying.circles.init.CMItems;
import com.lying.circles.init.CMStatusEffects;
import com.lying.circles.network.PacketHandler;
import com.lying.circles.network.PacketSyncSpellManager;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.CrucibleManager;
import com.lying.circles.utility.LeylineManager;
import com.lying.circles.utility.SpellManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerBus
{
	@SubscribeEvent
	public static void onAttachCapabilityEvent(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject().getType() == EntityType.PLAYER)
			event.addCapability(PlayerData.IDENTIFIER, new PlayerData((Player)event.getObject()));
		
		if(event.getObject() instanceof LivingEntity)
			event.addCapability(LivingData.IDENTIFIER, new LivingData((LivingEntity)event.getObject()));
	}
	
	@SubscribeEvent
	public static void onPlayerLogIn(PlayerLoggedInEvent event)
	{
		ServerPlayer player = (ServerPlayer)event.getEntity();
		PacketHandler.sendTo(player, new PacketSyncSpellManager(SpellManager.instance(player.getLevel())));
		
		PlayerData data = PlayerData.getCapability(player);
		if(data != null)
			data.markDirty();
	}
	
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.Clone event)
	{
		Player playerNew = event.getEntity();
		Player playerOld = event.getOriginal();
		playerOld.reviveCaps();
		
		PlayerData dataNew = PlayerData.getCapability(playerNew);
		PlayerData dataOld = PlayerData.getCapability(playerOld);
		if(dataNew != null && dataOld != null)
			dataNew.deserializeNBT(dataOld.serializeNBT());
		
		playerOld.invalidateCaps();
	}
	
	@SubscribeEvent
	public static void onPlayerDeathByCurruisis(LivingDeathEvent event)
	{
		if(event.getSource() == CMDamageSource.PETRIFICATION && event.getEntity().getType() == EntityType.PLAYER)
			PlayerData.getCapability((Player)event.getEntity()).flagPetrified();
	}
	
	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent event)
	{
		if(event.level.isClientSide() || event.phase == TickEvent.Phase.END)
			return;
		
		SpellManager manager = SpellManager.instance(event.level);
		if(!manager.isEmpty())
			manager.tick();
		
		LeylineManager leyLines = LeylineManager.instance(event.level);
		if(leyLines != null && !leyLines.isEmpty())
			leyLines.tick();
	}
	
	@SubscribeEvent
	public static void onLivingTick(LivingTickEvent event)
	{
		LivingEntity living = event.getEntity();
		
		LivingData data = LivingData.getCapability(living);
		data.tick(living.getLevel());
		
		if(living.getType() == EntityType.PLAYER)
		{
			PlayerData playerData = PlayerData.getCapability((Player)living);
			playerData.tick(living.getLevel());
		}
		
		LeylineManager leyLines = LeylineManager.instance(living.level);
		if(leyLines != null && leyLines.isOnLeyLine(living))
			living.addEffect(new MobEffectInstance(CMStatusEffects.LEY_POWER.get(), Reference.Values.TICKS_PER_SECOND * 5));
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingDeath(LivingDeathEvent event)
	{
		if(event.isCanceled())
			return;
		
		SpellManager.instance(event.getEntity().getLevel()).removeSpellsFrom(event.getEntity());
	}
	
	/**
	 * When block is placed, if block is crucible expansion, then notify manager
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockPlaced(EntityPlaceEvent event)
	{
		if(!event.isCanceled())
		{
			Level world = (Level)event.getLevel();
			CrucibleManager manager = CrucibleManager.instance(world);
			if(event.getPlacedBlock().getBlock() instanceof ICruciblePart)
				manager.addExpansionAt(event.getPos(), ((ICruciblePart)event.getPlacedBlock().getBlock()).partType(event.getPos(), event.getPlacedBlock(), world));
			if(event.getState().is(CMBlocks.CRUCIBLE.get()))
				manager.addCrucibleAt(event.getPos());
		}
	}
	
	/**
	 * Notifies managers to remove destroyed crucible expansion blocks
	 * @param event
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockBroken(BreakEvent event)
	{
		if(!event.isCanceled())
		{
			Level world = (Level)event.getLevel();
			CrucibleManager manager = CrucibleManager.instance(world);
			if(event.getState().getBlock() instanceof ICruciblePart)
				manager.removeExpansionAt(event.getPos());
			if(event.getState().is(CMBlocks.CRUCIBLE.get()))
				manager.removeCrucibleAt(event.getPos());
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onHoeSand(RightClickBlock event)
	{
		if(!event.isCanceled() && !event.getLevel().isClientSide())
		{
			ItemStack stack = event.getItemStack();
			if(!stack.canPerformAction(ToolActions.HOE_TILL))
				return;
			
			BlockPos pos = event.getPos();
			Level world = event.getLevel();
			BlockState block = world.getBlockState(pos);
			RegistryObject<Block> tilled = TilledSand.getTilledVersion(block.getBlock());
			if(tilled == null || !tilled.isPresent())
				return;
			
			event.setUseItem(Result.ALLOW);
			event.setUseBlock(Result.DENY);
			
			BlockState state = tilled.get().defaultBlockState();
			if(state.hasProperty(TilledSand.SHAPE_PROPERTY))
			{
				Shape tillShape = null;
				switch(Direction.orderedByNearest(event.getEntity())[0])
				{
					case EAST:
					case WEST:
						tillShape = Shape.EAST_WEST;
						break;
					case NORTH:
					case SOUTH:
					default:
						tillShape = Shape.NORTH_SOUTH;
						break;
				}
				state = state.setValue(TilledSand.SHAPE_PROPERTY, tillShape);
			}
			world.setBlockAndUpdate(pos, state);
			stack.hurt(1, world.getRandom(), (ServerPlayer)event.getEntity());
		}
	}
	
	@SubscribeEvent
	public static void onLichRespawn(PlayerEvent.Clone event)
	{
		if(!event.isWasDeath() || event.getEntity().getLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || !PlayerData.isLich(event.getEntity()))
			return;
		
		Player oP = event.getOriginal();
		Player nP = event.getEntity();
		
		Inventory oPInv = oP.getInventory();
		ItemStack skull = ItemStack.EMPTY;
		for(int i=0; i<oPInv.getContainerSize(); i++)
		{
			ItemStack stack = oPInv.getItem(i);
			if(stack.getItem() == CMItems.LICH_SKULL.get())
			{
				skull = stack;
				break;
			}
		}
		
		if(skull.isEmpty())
			skull = new ItemStack(CMItems.LICH_SKULL.get());
		
		nP.getInventory().add(skull);
	}
}
