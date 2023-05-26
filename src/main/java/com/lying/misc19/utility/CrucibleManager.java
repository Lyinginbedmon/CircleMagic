package com.lying.misc19.utility;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.lying.misc19.blocks.ICruciblePart;
import com.lying.misc19.blocks.ICruciblePart.PartType;
import com.lying.misc19.blocks.entity.CrucibleBlockEntity;
import com.lying.misc19.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec2;

/**
 * Tracks all known crucibles in the world as a convenient location database
 * @author Remem
 *
 */
public class CrucibleManager extends SavedData
{
	protected static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_crucible_manager";
	
	private List<BlockPos> activeCrucibles = Lists.newArrayList();
	private Map<PartType, List<BlockPos>> activeExpansions = new HashMap<>();
	
	private Level world;
	
	private CrucibleManager() { }
	
	@Nullable
	public static CrucibleManager instance(Level worldIn)
	{
		if(worldIn.isClientSide())
			return null;
		
		CrucibleManager manager = ((ServerLevel)worldIn).getDataStorage().computeIfAbsent(CrucibleManager::fromNbt, CrucibleManager::new, DATA_NAME);
		manager.world = worldIn;
		return manager;
	}
	
	public static CrucibleManager fromNbt(CompoundTag tag)
	{
		CrucibleManager manager = new CrucibleManager();
		manager.read(tag);
		return manager;
	}
	
	public CompoundTag save(CompoundTag data)
	{
		ListTag crucibles = new ListTag();
		activeCrucibles.forEach((block) -> crucibles.add(NbtUtils.writeBlockPos(block)));
		data.put("Crucibles", crucibles);
		
		ListTag expansions = new ListTag();
		for(PartType type : PartType.values())
		{
			ListTag entries = new ListTag();
			getExpansions(type).forEach((block) -> entries.add(NbtUtils.writeBlockPos(block)));
			expansions.add(entries);
		}
		data.put("Expansions", expansions);
		return data;
	}
	
	public void read(CompoundTag data)
	{
		this.activeCrucibles.clear();
		ListTag crucibles = data.getList("Crucibles", Tag.TAG_COMPOUND);
		for(int i=0; i<crucibles.size(); i++)
			activeCrucibles.add(NbtUtils.readBlockPos(crucibles.getCompound(i)));
		
		this.activeExpansions.clear();
		ListTag expansions = data.getList("Expansions", Tag.TAG_LIST);
		for(PartType type : PartType.values())
		{
			ListTag entries = expansions.getList(type.ordinal());
			List<BlockPos> pos = Lists.newArrayList();
			for(int i=0; i<entries.size(); i++)
				pos.add(NbtUtils.readBlockPos(entries.getCompound(i)));
			this.activeExpansions.put(type, pos);
		}
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
	
	public void addExpansionAt(BlockPos pos, PartType type)
	{
		List<BlockPos> expansions = getExpansions(type);
		if(!expansions.contains(pos))
		{
			expansions.add(pos);
			this.activeExpansions.put(type, expansions);
			setDirty();
		}
	}
	
	public void removeExpansionAt(BlockPos pos)
	{
		boolean found = false;
		for(PartType type : PartType.values())
		{
			List<BlockPos> values = this.activeExpansions.getOrDefault(type, Lists.newArrayList());
			if(values.remove(pos))
			{
				this.activeExpansions.put(type, values);
				found = true;
			}
		}
		
		if(found)
			setDirty();
	}
	
	public List<BlockPos> getExpansions(PartType type)
	{
		List<BlockPos> expansions = this.activeExpansions.getOrDefault(type, Lists.newArrayList());
		
		List<BlockPos> invalid = Lists.newArrayList();
		expansions.forEach((pos) -> 
		{
			Block block = world.getBlockState(pos).getBlock();
			if(!(block instanceof ICruciblePart))
				invalid.add(pos);
			
		});
		
		if(!invalid.isEmpty())
		{
			expansions.removeAll(invalid);
			this.activeExpansions.put(type, expansions);
			setDirty();
		}
		
		return expansions;
	}
	
	public List<BlockPos> getPartsOfType(PartType type, BlockPos pos)
	{
		return getPartsOfType(type, pos, (pos1, state, world, pos2) -> ((ICruciblePart)state.getBlock()).isPartValidFor(pos1, state, world, pos2));
	}
	
	public List<BlockPos> getPartsOfType(PartType type, BlockPos pos, PartValidator validatorIn)
	{
		List<BlockPos> parts = Lists.newArrayList();
		getExpansions(type).forEach((part) -> 
		{
			BlockState state = world.getBlockState(part);
			if(state.getBlock() instanceof ICruciblePart && Math.sqrt(part.distSqr(pos)) <= CrucibleBlockEntity.RANGE && validatorIn.validate(part, state, world, pos))
				parts.add(part);
		});
		return parts;
	}
	
	public Map<Integer, List<BlockPos>> getDelineatedPartsOfType(PartType type, BlockPos pos)
	{
		return delineatePartsAround(getPartsOfType(type, pos), pos);
	}
	
	/** Groups positions together based on the closest multiple of spaces from it to the crucible */
	public static Map<Integer, List<BlockPos>> delineatePartsAround(List<BlockPos> parts, BlockPos crucible)
	{
		Map<Integer, List<BlockPos>> delineated = new HashMap<>();
		Vec2 crucibleVec = new Vec2(crucible.getX() + 0.5F, crucible.getZ() + 0.5F);
		Comparator<BlockPos> sorter = sortClockwiseAround(crucible);
		
		// Collect all positions together based on their nearest multiple of spacing distance from the crucible
		for(BlockPos pillar : parts)
		{
			Vec2 pillarVec = new Vec2(pillar.getX() + 0.5F, pillar.getZ() + 0.5F);
			int ring = (int)Math.round(Math.sqrt(pillarVec.distanceToSqr(crucibleVec)) / CrucibleBlockEntity.SPACING);
			List<BlockPos> set = delineated.getOrDefault(ring, Lists.newArrayList());
			set.add(pillar);
			
			set.sort(sorter);
			delineated.put(ring, set);
		}
		
		return delineated;
	}
	
	public static Comparator<BlockPos> sortClockwiseAround(BlockPos pos)
	{
		Vec2 crucibleVec = new Vec2(pos.getX() + 0.5F, pos.getZ() + 0.5F);
		return new Comparator<BlockPos>()
		{
			public int compare(BlockPos pos1, BlockPos pos2)
			{
				Vec2 vec1 = new Vec2(pos1.getX(), pos1.getZ());
				Vec2 vec2 = new Vec2(pos2.getX(), pos2.getZ());
				double angle1 = Math.atan2(vec1.x, vec1.y) - Math.atan2(crucibleVec.x, crucibleVec.y);
				double angle2 = Math.atan2(vec2.x, vec2.y) - Math.atan2(crucibleVec.x, crucibleVec.y);
				return angle1 > angle2 ? 1 : angle1 < angle2 ? -1 : 0;
			}
		};
	}
}
