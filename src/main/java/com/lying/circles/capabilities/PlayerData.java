package com.lying.circles.capabilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lying.circles.client.ClientSetupEvents;
import com.lying.circles.init.CMCapabilities;
import com.lying.circles.reference.Reference;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerData implements ICapabilitySerializable<CompoundTag>
{
	public static final ResourceLocation IDENTIFIER = new ResourceLocation(Reference.ModInfo.MOD_ID, "player_data");
	
	private Player thePlayer;
	
	private int curruisisStage = 0;
	
	private boolean isDirty = true;
	
	public PlayerData(Player playerIn)
	{
		this.thePlayer = playerIn;
	}
	
	public void setPlayer(Player playerIn) { this.thePlayer = playerIn; }
	
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		return CMCapabilities.PLAYER_DATA.orEmpty(cap, LazyOptional.of(() -> this));
	}
	
	public static PlayerData getCapability(Player player)
	{
		if(player == null)
			return null;
		else if(player.getLevel().isClientSide())
			return ClientSetupEvents.getPlayerData(player);
		
		PlayerData data = player.getCapability(CMCapabilities.PLAYER_DATA).orElse(new PlayerData(player));
		data.thePlayer = player;
		return data;
	}
	
	public CompoundTag serializeNBT()
	{
		CompoundTag data = new CompoundTag();
		data.putInt("Curruisis", curruisisStage);
		return data;
	}
	
	public void deserializeNBT(CompoundTag nbt)
	{
		this.curruisisStage = nbt.getInt("Curruisis");
	}
	
	public int curruisis() { return this.curruisisStage; }
	
	public void markDirty() { this.isDirty = true; }
}
