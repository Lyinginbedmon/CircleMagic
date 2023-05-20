package com.lying.misc19.utility.bus;

import com.lying.misc19.blocks.ICruciblePart;
import com.lying.misc19.blocks.TilledSand;
import com.lying.misc19.blocks.TilledSand.Shape;
import com.lying.misc19.blocks.entity.CrucibleBlockEntity;
import com.lying.misc19.init.M19BlockEntities;
import com.lying.misc19.init.M19Blocks;
import com.lying.misc19.reference.Reference;
import com.lying.misc19.utility.CrucibleManager;
import com.lying.misc19.utility.SpellManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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
	 * When block is placed, if block is crucible expansion, then notify nearby crucibles
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockPlaced(EntityPlaceEvent event)
	{
		if(!event.isCanceled())
			if(event.getPlacedBlock().getBlock() instanceof ICruciblePart)
			{
				LevelAccessor world = event.getLevel();
				
				CrucibleManager manager = CrucibleManager.instance((Level)world);
				for(BlockPos crucible : manager.getCruciblesWithin(event.getPos(), 64D))
				{
					BlockEntity entity = world.getBlockEntity(crucible);
					if(entity == null || entity.getType() != M19BlockEntities.CRUCIBLE.get())
						continue;
					
					CrucibleBlockEntity crucibleEntity = (CrucibleBlockEntity)entity;
					crucibleEntity.assessAndAddExpansion(event.getPos());
				}
			}
			else if(event.getState().is(M19Blocks.CRUCIBLE.get()))
				CrucibleManager.instance((Level)event.getLevel()).addCrucibleAt(event.getPos());
	}
	
	/**
	 * Notifies crucibles to remove destroyed expansion blocks
	 * @param event
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockBroken(BreakEvent event)
	{
		if(!event.isCanceled())
			if(event.getState().getBlock() instanceof ICruciblePart)
			{
				LevelAccessor world = event.getLevel();
				
				CrucibleManager manager = CrucibleManager.instance((Level)world);
				for(BlockPos crucible : manager.getCruciblesWithin(event.getPos(), 16D))
				{
					BlockEntity entity = world.getBlockEntity(crucible);
					if(entity == null || entity.getType() != M19BlockEntities.CRUCIBLE.get())
						continue;
					
					CrucibleBlockEntity crucibleEntity = (CrucibleBlockEntity)entity;
					crucibleEntity.removeExpansion(event.getPos());
				}
			}
			else if(event.getState().is(M19Blocks.CRUCIBLE.get()))
				CrucibleManager.instance((Level)event.getLevel()).removeCrucibleAt(event.getPos());
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
