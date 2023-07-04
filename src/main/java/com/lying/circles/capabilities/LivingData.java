package com.lying.circles.capabilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lying.circles.client.ClientSetupEvents;
import com.lying.circles.init.CMCapabilities;
import com.lying.circles.init.CMDamageSource;
import com.lying.circles.init.CMStatusEffects;
import com.lying.circles.network.PacketHandler;
import com.lying.circles.network.PacketSyncLivingData;
import com.lying.circles.reference.Reference;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class LivingData implements ICapabilitySerializable<CompoundTag>
{
	public static final ResourceLocation IDENTIFIER = new ResourceLocation(Reference.ModInfo.MOD_ID, "living_data");
	private static final int INITIAL_CAPACITY = 1000;
	
	private LivingEntity theEntity;
	
	private int manaCapacity = INITIAL_CAPACITY;
	private int currentMana = manaCapacity;
	private int recoveryRate = 1;
	
	public LivingData(LivingEntity playerIn)
	{
		this.theEntity = playerIn;
	}
	
	public void setLiving(LivingEntity playerIn) { this.theEntity = playerIn; }
	
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		return CMCapabilities.LIVING_DATA.orEmpty(cap, LazyOptional.of(() -> this));
	}
	
	public static LivingData getCapability(LivingEntity player)
	{
		if(player == null)
			return null;
		else if(player.getLevel().isClientSide())
			return ClientSetupEvents.getLivingData(player);
		
		LivingData data = player.getCapability(CMCapabilities.LIVING_DATA).orElse(new LivingData(player));
		data.theEntity = player;
		return data;
	}
	
	public CompoundTag serializeNBT()
	{
		CompoundTag data = new CompoundTag();
		
		data.putInt("Mana", this.currentMana);
		data.putInt("Capacity", manaCapacity);
		data.putInt("Recovery", recoveryRate);
		
		return data;
	}
	
	public void deserializeNBT(CompoundTag nbt)
	{
		this.currentMana = nbt.getInt("Mana");
		this.manaCapacity = nbt.getInt("Capacity");
		this.recoveryRate = nbt.getInt("Recovery");
	}
	
	public void tick(Level worldIn)
	{
		if(!this.theEntity.isAlive())
			return;
		
		int capacity = getCurrentCapacity();
		if(currentMana < capacity || capacity < 0)
		{
			currentMana += getRecoveryRate();
			markDirty();
		}
		else if(currentMana > capacity && capacity > 0)
		{
			currentMana = manaCapacity;
			markDirty();
		}
		
		if(currentMana < 0)
		{
			this.theEntity.hurt(CMDamageSource.OUT_OF_MANA, Math.abs(currentMana - 1));
			this.currentMana = 1;
			markDirty();
		}
		else if(currentMana > this.manaCapacity * 2 && !this.theEntity.isInvulnerableTo(CMDamageSource.TOO_MUCH_MANA))
		{
			resetMana();
			this.theEntity.hurt(CMDamageSource.TOO_MUCH_MANA, Float.MAX_VALUE);
			
			Level world = this.theEntity.level;
			Explosion.BlockInteraction blockInteraction = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, this.theEntity) ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE;
			world.explode(this.theEntity, this.theEntity.getX(), this.theEntity.getY(), this.theEntity.getZ(), 3F, blockInteraction);
		}
	}
	
	public void resetMana() { this.currentMana = this.manaCapacity; markDirty(); }
	
	public int getRecoveryRate()
	{
		int bonus = this.recoveryRate;
		if(hasLeyPower())
			bonus += this.theEntity.getEffect(CMStatusEffects.LEY_POWER.get()).getAmplifier() + 1;
		return bonus;
	}
	
	public int getCurrentCapacity()
	{
		return hasLeyPower() ? -1 : this.manaCapacity;
	}
	
	private boolean hasLeyPower() { return this.theEntity.hasEffect(CMStatusEffects.LEY_POWER.get()) && this.theEntity.getEffect(CMStatusEffects.LEY_POWER.get()).getDuration() > 0; }
	
	public void markDirty()
	{
		if(!this.theEntity.getLevel().isClientSide())
			PacketHandler.sendToAll((ServerLevel)this.theEntity.getLevel(), new PacketSyncLivingData(this.theEntity.getUUID(), this.theEntity.position(), this.serializeNBT()));
	}
}
