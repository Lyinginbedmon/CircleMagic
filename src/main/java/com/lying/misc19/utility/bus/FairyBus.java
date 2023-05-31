package com.lying.misc19.utility.bus;

import java.util.Optional;
import java.util.function.Function;

import com.lying.misc19.blocks.ICruciblePart.PartType;
import com.lying.misc19.blocks.entity.FairyJarBlockEntity;
import com.lying.misc19.blocks.entity.FairyJarBlockEntity.EmotiveEvent;
import com.lying.misc19.init.M19BlockEntities;
import com.lying.misc19.init.M19Blocks;
import com.lying.misc19.reference.Reference;
import com.lying.misc19.utility.CrucibleManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FairyBus
{
	@SubscribeEvent
	public static void onShakeJar(final LeftClickBlock event)
	{
		Player player = event.getEntity();
		Level world = player.getLevel();
		if(!world.isClientSide() && world.getBlockState(event.getPos()).getBlock() == M19Blocks.FAIRY_JAR.get())
		{
			BlockPos pos = event.getPos();
			addToAllFairies(world, (fairy) -> pos.equals(fairy.getBlockPos()) ? EmotiveEvent.SHAKE_JAR_OWN : EmotiveEvent.SHAKE_JAR_OTHER);
		}
	}
	
	@SubscribeEvent
	public static void onMobHurt(final LivingHurtEvent event)
	{
		LivingEntity mob = event.getEntity();
		if(!mob.getLevel().isClientSide())
			addToAllFairies(mob.getLevel(), (fairy) -> mob.getType() == EntityType.PLAYER ? (fairy.isOwner(mob) ? EmotiveEvent.OWNER_HURT : EmotiveEvent.PLAYER_HURT) : EmotiveEvent.MOB_HURT);
	}
	
	@SubscribeEvent
	public static void onJarPlaced(final BlockEvent.EntityPlaceEvent event)
	{
		if(!event.getLevel().isClientSide() && event.getPlacedBlock().getBlock() == M19Blocks.FAIRY_JAR.get())
		{
			Optional<FairyJarBlockEntity> fairy = event.getLevel().getBlockEntity(event.getPos(), M19BlockEntities.FAIRY_JAR.get());
			if(fairy.isPresent())
			{
				if(fairy.get().lastPlaced().distSqr(event.getPos()) > (16D * 16D))
					fairy.get().addEmotiveEvent(EmotiveEvent.TRAVEL);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static void addToAllFairies(Level world, EmotiveEvent event)
	{
		addToAllFairies(world, (fairy) -> event);
	}
	
	private static void addToAllFairies(Level world, Function<FairyJarBlockEntity,EmotiveEvent> event)
	{
		CrucibleManager manager = CrucibleManager.instance(world);
		for(BlockPos fairy : manager.getExpansions(PartType.FAIRY))
		{
			Optional<FairyJarBlockEntity> fairyJar = world.getBlockEntity(fairy, M19BlockEntities.FAIRY_JAR.get());
			if(fairyJar.isPresent())
				fairyJar.get().addEmotiveEvent(event.apply(fairyJar.get()));
		}
	}
}