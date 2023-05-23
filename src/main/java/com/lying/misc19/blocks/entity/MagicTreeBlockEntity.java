package com.lying.misc19.blocks.entity;

import com.lying.misc19.init.M19BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MagicTreeBlockEntity extends BlockEntity implements Container
{
	protected ItemStack stack = ItemStack.EMPTY;
	
	private double ticksActive = 0;
	
	public MagicTreeBlockEntity(BlockPos pos, BlockState state)
	{
		super(M19BlockEntities.MAGIC_TREE.get(), pos, state);
	}
	
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.put("Item", stack.save(new CompoundTag()));
	}
	
	public void load(CompoundTag compound)
	{
		super.load(compound);
		this.stack = ItemStack.of(compound.getCompound("Item"));
	}
	
	public static void tickClient(Level world, BlockPos pos, BlockState state, MagicTreeBlockEntity tile)
	{
		RandomSource randomsource = world.getRandom();
		double d0 = (double)pos.getX() + randomsource.nextDouble();
		double d1 = (double)pos.getY() + randomsource.nextDouble() * 2D;
		double d2 = (double)pos.getZ() + randomsource.nextDouble();
		world.addParticle(ParticleTypes.WITCH, d0, d1, d2, 0.0D, 0.0D, 0.0D);
	}
	
	public static void tickServer(Level world, BlockPos pos, BlockState state, MagicTreeBlockEntity tile)
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
	
	public int getContainerSize() { return 1; }
	
	public ItemStack getItem(int slot) { return stack; }
	
	public void clearContent() { this.stack = ItemStack.EMPTY; }
	
	public void setItem(int slot, ItemStack item) { this.stack = item; }
	
	public boolean isEmpty() { return this.stack.isEmpty(); }
	
	public ItemStack removeItem(int slot, int amount)
	{
		ItemStack copy = this.stack.copy().split(amount);
		this.stack.shrink(amount);
		return copy;
	}
	
	public ItemStack removeItemNoUpdate(int p_18951_)
	{
		ItemStack copy = this.stack.copy();
		this.stack = ItemStack.EMPTY;
		return copy;
	}
	
	public boolean stillValid(Player p_18946_) { return false; }
}
