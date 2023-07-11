package com.lying.circles.blocks;

import com.lying.circles.capabilities.LivingData;
import com.lying.circles.utility.ManaReserve;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ManaCrystal extends Block
{
	private static final float PER_MANA = 30F;
	
	public ManaCrystal(Properties p_49795_)
	{
		super(p_49795_);
	}
	
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if(!world.isClientSide())
		{
			ManaReserve reserve = ManaReserve.instance(world);
			LivingData living = LivingData.getCapability(player);
			if(living.getCurrentMana() > PER_MANA)
			{
				float total = reserve.addManaTo(player.getUUID(), 1);
				living.spendMana(PER_MANA);
				player.displayClientMessage(Component.literal("Mana in reserve: "+total), true);
			}
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}
}