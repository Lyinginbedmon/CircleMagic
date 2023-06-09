package com.lying.circles.utility.bus;

import java.util.Optional;
import java.util.function.Function;

import com.lying.circles.blocks.ICruciblePart.PartType;
import com.lying.circles.blocks.entity.FairyJarBlockEntity;
import com.lying.circles.blocks.entity.FairyPersonalityModel.EmotiveEvent;
import com.lying.circles.init.CMBlockEntities;
import com.lying.circles.init.CMBlocks;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.CrucibleManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
		if(!world.isClientSide() && world.getBlockState(event.getPos()).getBlock() == CMBlocks.FAIRY_JAR.get())
		{
			BlockPos pos = event.getPos();
			Vec3 position = event.getEntity().getEyePosition();
			addToAllFairies(world, position, (fairy) -> pos.equals(fairy.getBlockPos()) ? EmotiveEvent.SHAKE_JAR_OWN : EmotiveEvent.SHAKE_JAR_OTHER);
		}
	}
	
	@SubscribeEvent
	public static void onMobHurt(final LivingHurtEvent event)
	{
		LivingEntity mob = event.getEntity();
		if(!mob.getLevel().isClientSide())
			addToAllFairies(mob.getLevel(), event.getEntity().position().add(0D, event.getEntity().getBbHeight() / 2, 0D), (fairy) -> mob.getType() == EntityType.PLAYER ? (fairy.isOwner(mob) ? EmotiveEvent.OWNER_HURT : EmotiveEvent.PLAYER_HURT) : EmotiveEvent.MOB_HURT);
	}
	
	@SubscribeEvent
	public static void onJarPlaced(final BlockEvent.EntityPlaceEvent event)
	{
		if(!event.getLevel().isClientSide() && event.getPlacedBlock().getBlock() == CMBlocks.FAIRY_JAR.get())
		{
			Optional<FairyJarBlockEntity> fairy = event.getLevel().getBlockEntity(event.getPos(), CMBlockEntities.FAIRY_JAR.get());
			if(fairy.isPresent())
			{
				if(fairy.get().lastPlaced().distSqr(event.getPos()) > (16D * 16D))
					fairy.get().addEmotiveEvent(EmotiveEvent.TRAVEL);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static void addToAllFairies(Level world, Vec3 position, EmotiveEvent event)
	{
		addToAllFairies(world, position, (fairy) -> event);
	}
	
	private static void addToAllFairies(Level world, Vec3 position, Function<FairyJarBlockEntity,EmotiveEvent> event)
	{
		CrucibleManager manager = CrucibleManager.instance(world);
		for(BlockPos fairy : manager.getExpansions(PartType.FAIRY))
		{
			Optional<FairyJarBlockEntity> fairyJar = world.getBlockEntity(fairy, CMBlockEntities.FAIRY_JAR.get());
			if(fairyJar.isPresent())
				fairyJar.get().addEmotiveEvent(event.apply(fairyJar.get()), position);
		}
	}
}
