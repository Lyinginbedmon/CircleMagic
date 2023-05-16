package com.lying.misc19.utility;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.lying.misc19.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class CrucibleManager extends SavedData
{
	protected static CrucibleManager INSTANCE = null;
	protected static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_crucible_manager";
	
	private List<BlockPos> activeCrucibles = Lists.newArrayList();
	
	@SuppressWarnings("unused")
	private Level world;
	
	private CrucibleManager() { this(null); }
	public CrucibleManager(@Nullable Level worldIn)
	{
		this.world = worldIn;
	}
	
	@Nullable
	public static CrucibleManager instance(Level worldIn)
	{
		if(worldIn.isClientSide())
			return null;
		
		if(INSTANCE == null)
		{
			ServerLevel overworld = ((ServerLevel)worldIn).getServer().getLevel(Level.OVERWORLD);
			INSTANCE = overworld.getDataStorage().computeIfAbsent(CrucibleManager::fromNbt, CrucibleManager::new, DATA_NAME);
			INSTANCE.world = worldIn;
		}
		return INSTANCE;
	}
	
	public static CrucibleManager fromNbt(CompoundTag tag)
	{
		CrucibleManager manager = new CrucibleManager();
		manager.read(tag);
		return manager;
	}
	
	public CompoundTag save(CompoundTag data)
	{
		ListTag expansions = new ListTag();
		activeCrucibles.forEach((block) -> expansions.add(NbtUtils.writeBlockPos(block)));
		data.put("Expansions", expansions);
		return data;
	}
	
	public void read(CompoundTag data)
	{
		this.activeCrucibles.clear();
		ListTag expansions = data.getList("Expansions", Tag.TAG_COMPOUND);
		for(int i=0; i<expansions.size(); i++)
			activeCrucibles.add(NbtUtils.readBlockPos(expansions.getCompound(i)));
	}
	
	public void addCrucibleAt(BlockPos pos)
	{
		if(!this.activeCrucibles.contains(pos))
		{
			this.activeCrucibles.add(pos);
			setDirty();
		}
	}
	
	public void removeCrucibleAt(BlockPos pos) { this.activeCrucibles.remove(pos); setDirty(); }
	
	public List<BlockPos> getCruciblesWithin(BlockPos pos, double maxRadius)
	{
		List<BlockPos> crucibles = Lists.newArrayList();
		this.activeCrucibles.forEach((block) -> { if(block.distSqr(pos) <= maxRadius * maxRadius) crucibles.add(block); });
		return crucibles;
	}
}
