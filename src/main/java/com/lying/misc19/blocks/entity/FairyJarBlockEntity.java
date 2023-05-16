package com.lying.misc19.blocks.entity;

import com.lying.misc19.init.M19BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FairyJarBlockEntity extends BlockEntity
{
	private double ticksActive = 0;
	
	public FairyJarBlockEntity(BlockPos pos, BlockState state)
	{
		super(M19BlockEntities.FAIRY_JAR.get(), pos, state);
	}
	
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
	}
	
	public void load(CompoundTag compound)
	{
		super.load(compound);
	}
	
	public static void tickClient(Level world, BlockPos pos, BlockState state, FairyJarBlockEntity tile)
	{
		tile.ticksActive++;
	}
	
	public static void tickServer(Level world, BlockPos pos, BlockState state, FairyJarBlockEntity tile)
	{
		
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
