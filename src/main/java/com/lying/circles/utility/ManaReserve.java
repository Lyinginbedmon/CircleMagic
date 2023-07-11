package com.lying.circles.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import com.lying.circles.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class ManaReserve extends SavedData
{
	protected static ManaReserve INSTANCE = null;
	protected static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_mana_reserve";
	
	private Map<UUID, Float> reserves = new HashMap<>();
	
	@Nullable
	public static ManaReserve instance(Level worldIn)
	{
		if(INSTANCE == null && !worldIn.isClientSide())
		{
			ServerLevel overworld = ((ServerLevel)worldIn).getServer().getLevel(Level.OVERWORLD);
			INSTANCE = overworld.getDataStorage().computeIfAbsent(ManaReserve::fromNbt, ManaReserve::new, DATA_NAME);
		}
		return INSTANCE;
	}
	
	public static ManaReserve fromNbt(CompoundTag tag)
	{
		ManaReserve manager = new ManaReserve();
		manager.read(tag);
		return manager;
	}
	
	public CompoundTag save(CompoundTag nbt)
	{
		ListTag list = new ListTag();
		for(Entry<UUID, Float> entry : reserves.entrySet())
		{
			if(entry.getValue() <= 0)
				continue;
			
			CompoundTag data = new CompoundTag();
			data.putUUID("ID", entry.getKey());
			data.putFloat("Amount", entry.getValue());
			list.add(data);
		}
		nbt.put("Values", list);
		return nbt;
	}
	
	public void read(CompoundTag compound)
	{
		reserves.clear();
		ListTag list = compound.getList("Values", Tag.TAG_COMPOUND);
		for(int i=0; i<list.size(); i++)
		{
			CompoundTag data = list.getCompound(i);
			reserves.put(data.getUUID("ID"), data.getFloat("Amount"));
		}
	}
	
	public float getManaFor(UUID uuidIn) { return reserves.getOrDefault(uuidIn, 0F); }
	
	public float addManaTo(UUID uuidIn, float par2Float)
	{
		float amount = getManaFor(uuidIn) + par2Float;
		reserves.put(uuidIn, amount);
		setDirty();
		return amount;
	}
}
