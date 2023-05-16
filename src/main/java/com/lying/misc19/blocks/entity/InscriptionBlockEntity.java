package com.lying.misc19.blocks.entity;

import com.lying.misc19.blocks.InscribedBlock;
import com.lying.misc19.init.M19BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class InscriptionBlockEntity extends BlockEntity
{
	private double ticksActive = 0;
	
	public InscriptionBlockEntity(BlockPos pos, BlockState state)
	{
		super(M19BlockEntities.INSCRIPTION.get(), pos, state);
	}
	
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
	}
	
	public void load(CompoundTag compound)
	{
		super.load(compound);
	}
	
	public boolean isValid()
	{
		return ((InscribedBlock)getBlockState().getBlock()).isActive(getBlockPos(), getLevel());
	}
	
	public static void tickClient(Level world, BlockPos pos, BlockState state, InscriptionBlockEntity tile)
	{
		tile.ticksActive++;
		if(tile.isValid())
		{
			RandomSource rand = world.getRandom();
			double d0 = pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 1.3D;
			double d1 = pos.getY() + rand.nextDouble();
			double d2 = pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 1.3D;
			world.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}
	
	public double renderTicks() { return ticksActive; }
	
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	public CompoundTag getUpdateTag()
	{
		CompoundTag compound = new CompoundTag();
		saveAdditional(compound);
		return compound;
	}
}
