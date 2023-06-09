package com.lying.circles.blocks.entity;

import com.lying.circles.client.particle.M19Particles;
import com.lying.circles.init.CMBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MagicTreeBlockEntity extends BlockEntity implements Container
{
	protected ItemStack stack = ItemStack.EMPTY;
	
	private double ticksActive = 0;
	
	public MagicTreeBlockEntity(BlockPos pos, BlockState state)
	{
		super(CMBlockEntities.MAGIC_TREE.get(), pos, state);
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
		tile.ticksActive++;
		
		RandomSource rand = world.random;
		if(!tile.stack.isEmpty() && rand.nextInt(5) == 0)
		{
			for(int i=0; i<rand.nextInt(4); i++)
			{
				float spin = (float)Math.floorDiv((int)Math.toDegrees(rand.nextFloat()), 4) * 4F;
				Vec3 turn = new Vec3(0, 0, 0.265D).yRot(spin);
				Vec3 position = new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D).add(turn);
				world.addParticle(M19Particles.SQUARES.get(), position.x, position.y, position.z, 0, 1, 0);
			}
		}
	}
	
	public static void tickServer(Level world, BlockPos pos, BlockState state, MagicTreeBlockEntity tile)
	{
		
	}
	
	public double renderTicks() { return ticksActive; }
	
	public void interactWithItem(Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		ItemStack heldStack = player.getItemInHand(hand);
		if(!isEmpty())
		{
			dropContainedItem(getLevel(), player.getX(), player.getY(), player.getZ(), stack);
			clearContent();
		}
		
		if(!heldStack.isEmpty())
			setStack(player.getInventory().removeItem(player.getInventory().selected, 1));
	}
	
	protected void dropContainedItem(Level world, double x, double y, double z, ItemStack stack)
	{
		world.addFreshEntity(new ItemEntity(world, getBlockPos().getX() + 0.5D, getBlockPos().getY() + 1.35D, getBlockPos().getZ() + 0.5D, stack));
	}
	
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
	
	public ItemStack getStack() { return getItem(0); }
	
	public void setStack(ItemStack item) { setItem(0, item); }
	
	public ItemStack removeStack(int amount) { return removeItem(0, amount); }
	
	public ItemStack getItem(int slot) { return stack; }
	
	public void clearContent()
	{
		this.stack = ItemStack.EMPTY;
		markDirty();
	}
	
	public void setItem(int slot, ItemStack item)
	{
		this.stack = item;
		markDirty();
	}
	
	public boolean isEmpty() { return this.stack.isEmpty(); }
	
	public ItemStack removeItem(int slot, int amount)
	{
		ItemStack copy = this.stack.copy().split(amount);
		this.stack.shrink(amount);
		markDirty();
		return copy;
	}
	
	public ItemStack removeItemNoUpdate(int p_18951_)
	{
		ItemStack copy = this.stack.copy();
		this.stack = ItemStack.EMPTY;
		return copy;
	}
	
	public boolean stillValid(Player p_18946_) { return false; }
	
	public void markDirty()
	{
		if(getLevel() != null)
		{
			BlockState state = getBlockState();
			getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);
			setChanged();
		}
	}
}
