package com.lying.circles.blocks;

import javax.annotation.Nullable;

import com.lying.circles.blocks.entity.LeyPointBlockEntity;
import com.lying.circles.init.CMBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class LeyPoint extends Block implements EntityBlock
{
	public LeyPoint(Properties propertiesIn)
	{
		super(propertiesIn);
	}
	
	public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
	
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new LeyPointBlockEntity(pos, state);
	}
	
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand)
	{
		for(int i = 0; i < 4; ++i)
		{
			double posX = (double)pos.getX() + rand.nextDouble();
			double posY = (double)pos.getY() + rand.nextDouble();
			double posZ = (double)pos.getZ() + rand.nextDouble();
			double velX = ((double)rand.nextFloat() - 0.5D) * 0.5D;
			double velY = ((double)rand.nextFloat() - 0.5D) * 0.5D;
			double velZ = ((double)rand.nextFloat() - 0.5D) * 0.5D;
			
			int j = rand.nextInt(2) * 2 - 1;
			if (!world.getBlockState(pos.west()).is(this) && !world.getBlockState(pos.east()).is(this))
			{
				posX = (double)pos.getX() + 0.5D + 0.25D * (double)j;
				velX = (double)(rand.nextFloat() * 2.0F * (float)j);
			}
			else
			{
				posZ = (double)pos.getZ() + 0.5D + 0.25D * (double)j;
				velZ = (double)(rand.nextFloat() * 2.0F * (float)j);
			}
			
			world.addParticle(ParticleTypes.PORTAL, posX, posY, posZ, velX, velY, velZ);
		}
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
	{
		return createTickerHelper(type, CMBlockEntities.LEY_POINT.get(), world.isClientSide() ? LeyPointBlockEntity::tickClient : LeyPointBlockEntity::tickServer);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_)
	{
		return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
	}
}
