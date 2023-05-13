package com.lying.misc19.blocks.entity;

import com.lying.misc19.init.M19BlockEntities;
import com.lying.misc19.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PhantomBlockEntity extends BlockEntity
{
	private double ticksActive = 0;
	private int duration = Reference.Values.TICKS_PER_SECOND * 20;
	
	public PhantomBlockEntity(BlockPos pos, BlockState state)
	{
		super(M19BlockEntities.PHANTOM_CUBE.get(), pos, state);
	}
	
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.putInt("Duration", duration);
	}
	
	public void load(CompoundTag compound)
	{
		super.load(compound);
		this.duration = compound.getInt("Duration");
	}
	
	public static void tickClient(Level world, BlockPos pos, BlockState state, PhantomBlockEntity tile)
	{
		tile.ticksActive++;
	}
	
	public static void tickServer(Level world, BlockPos pos, BlockState state, PhantomBlockEntity tile)
	{
		if(tile.duration <= 0)
			world.destroyBlock(pos, false);
		else
		{
			tile.duration = tile.duration - world.random.nextInt(2);
			tile.markUpdated();
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
		compound.putInt("Duration", this.duration);
		return compound;
	}
	
	private void markUpdated()
	{
		setChanged();
		getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
	}
}
