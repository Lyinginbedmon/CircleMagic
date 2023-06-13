package com.lying.circles.utility.bus;

import com.lying.circles.blocks.ICruciblePart;
import com.lying.circles.blocks.TilledSand;
import com.lying.circles.blocks.TilledSand.Shape;
import com.lying.circles.init.CMBlocks;
import com.lying.circles.network.PacketHandler;
import com.lying.circles.network.PacketSyncSpellManager;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.CrucibleManager;
import com.lying.circles.utility.SpellManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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
	public static void onPlayerLogIn(PlayerLoggedInEvent event)
	{
		ServerPlayer player = (ServerPlayer)event.getEntity();
		PacketHandler.sendTo(player, new PacketSyncSpellManager(SpellManager.instance(player.getLevel())));
	}
	
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
}
