package com.lying.misc19.blocks;

import com.lying.misc19.init.M19Items;
import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CurruidDust extends CarpetBlock
{
	public static final Vector3f PARTICLE_COLOR = new Vector3f(new Vec3(0, 0.5D, 1D));
	
	public CurruidDust(Properties propertiesIn)
	{
		super(propertiesIn.noLootTable());
	}
	
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random)
	{
		super.animateTick(state, world, pos, random);
		if(random.nextInt(10) == 0)
			for(int i=0; i<3; i++)
				world.addParticle(new DustParticleOptions(PARTICLE_COLOR, 1.0F), (double)pos.getX() + random.nextDouble(), (double)pos.getY() + 0D, (double)pos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
	}
	
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		ItemStack heldItem = player.getItemInHand(hand);
		if(heldItem.getItem() == Items.GLASS_BOTTLE)
		{
			if(!world.isClientSide())
			{
				world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
				player.addItem(new ItemStack(M19Items.CURRUID_DUST.get()));
			}
			return InteractionResult.sidedSuccess(world.isClientSide());
		}
		
		return InteractionResult.FAIL;
	}
	
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
	{
		BlockState blockstate = world.getBlockState(pos.below());
		return Block.isFaceFull(blockstate.getCollisionShape(world, pos.below()), Direction.UP);
	}
	
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return canSurvive(defaultBlockState(), context.getLevel(), context.getClickedPos()) ? defaultBlockState() : null;
	}
}
