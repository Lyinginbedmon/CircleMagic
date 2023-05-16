package com.lying.misc19.blocks.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.compress.utils.Lists;

import com.lying.misc19.blocks.ICruciblePart;
import com.lying.misc19.blocks.ICruciblePart.PartType;
import com.lying.misc19.init.M19BlockEntities;
import com.lying.misc19.init.SpellComponents;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.variable.VariableSet.Slot;
import com.lying.misc19.reference.Reference;
import com.lying.misc19.utility.CrucibleManager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CrucibleBlockEntity extends BlockEntity
{
	private Map<PartType, List<BlockPos>> expansionMap = new HashMap<>();
	private boolean hasNotifiedManager = false;
	
	private ISpellComponent arrangement = SpellComponents.create(SpellComponents.ROOT_DUMMY).addOutputs(SpellComponents.create(SpellComponents.CIRCLE_BASIC).addOutputs(
			SpellComponents.create(SpellComponents.GLYPH_SET).addInputs(SpellComponents.create(SpellComponents.SIGIL_FALSE)).addOutputs(SpellComponents.create(Slot.BAST.glyph())),
			SpellComponents.create(SpellComponents.GLYPH_SET).addInputs(SpellComponents.create(SpellComponents.SIGIL_TRUE)).addOutputs(SpellComponents.create(Slot.THOTH.glyph())),
			SpellComponents.create(SpellComponents.GLYPH_SET).addInputs(SpellComponents.create(SpellComponents.SIGIL_FALSE)).addOutputs(SpellComponents.create(Slot.SUTEKH.glyph())),
			SpellComponents.create(SpellComponents.GLYPH_XOR).addInputs(SpellComponents.create(Slot.BAST.glyph()), SpellComponents.create(Slot.THOTH.glyph())).addOutputs(SpellComponents.create(Slot.ANUBIS.glyph())),
			SpellComponents.create(SpellComponents.GLYPH_AND).addInputs(SpellComponents.create(Slot.ANUBIS.glyph()), SpellComponents.create(Slot.SUTEKH.glyph())).addOutputs(SpellComponents.create(Slot.HORUS.glyph())),
			SpellComponents.create(SpellComponents.GLYPH_AND).addInputs(SpellComponents.create(Slot.BAST.glyph()), SpellComponents.create(Slot.THOTH.glyph())).addOutputs(SpellComponents.create(Slot.ISIS.glyph())),
			SpellComponents.create(SpellComponents.GLYPH_OR).addInputs(SpellComponents.create(Slot.HORUS.glyph()), SpellComponents.create(Slot.ISIS.glyph())).addOutputs(SpellComponents.create(Slot.RA.glyph())),
			SpellComponents.create(SpellComponents.GLYPH_XOR).addInputs(SpellComponents.create(Slot.ANUBIS.glyph()), SpellComponents.create(Slot.SUTEKH.glyph())).addOutputs(SpellComponents.create(Slot.OSIRIS.glyph()))));
	
	public CrucibleBlockEntity(BlockPos pos, BlockState state)
	{
		super(M19BlockEntities.CRUCIBLE.get(), pos, state);
	}
	
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		ListTag expansions = new ListTag();
		for(PartType type : PartType.values())
		{
			ListTag entries = new ListTag();
			getExpansions(type).forEach((block) -> entries.add(NbtUtils.writeBlockPos(block)));
			expansions.add(entries);
		}
		compound.put("Expansions", expansions);
	}
	
	public void load(CompoundTag compound)
	{
		super.load(compound);
		this.expansionMap.clear();
		ListTag expansions = compound.getList("Expansions", Tag.TAG_LIST);
		for(PartType type : PartType.values())
		{
			ListTag entries = expansions.getList(type.ordinal());
			List<BlockPos> pos = Lists.newArrayList();
			for(int i=0; i<entries.size(); i++)
				pos.add(NbtUtils.readBlockPos(entries.getCompound(i)));
			this.expansionMap.put(type, pos);
		}
	}
	
	public static void tickClient(Level world, BlockPos pos, BlockState state, CrucibleBlockEntity tile)
	{
		;
	}
	
	public static void tickServer(Level world, BlockPos pos, BlockState state, CrucibleBlockEntity tile)
	{
		if(!tile.hasNotifiedManager)
		{
			CrucibleManager.instance(world).addCrucibleAt(pos);
			tile.hasNotifiedManager = true;
		}
		
		if(world.getGameTime() % Reference.Values.TICKS_PER_SECOND == 0)
		{
			System.out.println("# Crucible status #");
			System.out.println(" # Glyph cap: "+tile.glyphCap()+" ("+tile.getExpansions(PartType.PILLAR).size()+" pillars)");
			System.out.println(" # Suggestions: "+tile.hasSuggestions()+" ("+tile.getExpansions(PartType.FAIRY).size()+" fairies)");
			System.out.println(" # Holder points: "+tile.getExpansions(PartType.BOUGH).size());
		}
	}
	
	public ISpellComponent arrangement()
	{
		return this.arrangement;
	}
	
	public List<BlockPos> getExpansions(PartType type) { return this.expansionMap.getOrDefault(type, Lists.newArrayList()); }
	
	/** Retrieves all currently-valid positions of the given type, clears any that can't be valid any more */
	protected List<BlockPos> getValidOfType(PartType type)
	{
		List<BlockPos> validated = Lists.newArrayList();
		
		List<BlockPos> invalidated = Lists.newArrayList();
		List<BlockPos> points = getExpansions(type);
		for(BlockPos pos : points)
		{
			BlockState state = getLevel().getBlockState(pos);
			if(state.getBlock() instanceof ICruciblePart)
			{
				ICruciblePart part = (ICruciblePart)state.getBlock();
				if(part.isPartValidFor(pos, state, getLevel(), getBlockPos()))
					validated.add(pos);
			}
			else
				invalidated.add(pos);
		}
		
		if(!invalidated.isEmpty())
		{
			points.removeAll(invalidated);
			this.expansionMap.put(type, points);
			setChanged();
		}
		return validated;
	}
	
	public int glyphCap()
	{
		int cap = 5;
		for(BlockPos pos : getValidOfType(PartType.PILLAR))
		{
			BlockState state = getLevel().getBlockState(pos);
			ICruciblePart part = (ICruciblePart)state.getBlock();
			cap += part.glyphCapBonus(pos, state, getLevel(), getBlockPos());
		}
		return cap;
	}
	
	public boolean hasSuggestions()
	{
		boolean result = false;
		for(BlockPos pos : getValidOfType(PartType.FAIRY))
		{
			BlockState state = getLevel().getBlockState(pos);
			ICruciblePart part = (ICruciblePart)state.getBlock();
			result = result || part.canProvideSuggestions(pos, state, getLevel(), getBlockPos());
		}
		return result;
	}
	
	public void assessAndAddExpansion(BlockPos pos)
	{
		BlockState state = getLevel().getBlockState(pos);
		if(state.getBlock() instanceof ICruciblePart)
		{
			ICruciblePart part = (ICruciblePart)state.getBlock();
			PartType type = part.partType(pos, state, getLevel());
			List<BlockPos> partsOfType = this.expansionMap.getOrDefault(type, Lists.newArrayList());
			if(!partsOfType.contains(pos))
			{
				partsOfType.add(pos);
				this.expansionMap.put(type, partsOfType);
				setChanged();
			}
		}
	}
	
	public void removeExpansion(BlockPos pos)
	{
		boolean found = false;
		Map<PartType, List<BlockPos>> nextMap = new HashMap<>();
		for(Entry<PartType, List<BlockPos>> entry : this.expansionMap.entrySet())
		{
			PartType type = entry.getKey();
			List<BlockPos> points = entry.getValue();
			if(points.contains(pos))
			{
				points.remove(pos);
				found = true;
			}
			nextMap.put(type, points);
		}
		
		if(found)
		{
			this.expansionMap.clear();
			this.expansionMap.putAll(nextMap);
			setChanged();
		}
	}
	
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	public CompoundTag getUpdateTag()
	{
		CompoundTag compound = new CompoundTag();
		this.saveAdditional(compound);
		return compound;
	}
}
