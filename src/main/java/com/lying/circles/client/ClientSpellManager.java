package com.lying.circles.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.utility.EntityData;
import com.lying.circles.utility.SpellData;
import com.lying.circles.utility.SpellManager;
import com.lying.circles.utility.SpellTextureManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientSpellManager extends SpellManager
{
	public Map<UUID, Canvas> CANVAS_MAP = new HashMap<>();
	
	public ClientSpellManager(Level worldIn)
	{
		super(worldIn);
	}
	
	public CompoundTag save(CompoundTag data)
	{
		return data;
	}
	
	public void read(CompoundTag data)
	{
		super.read(data);
		
		// Identify all incoming UUIDs
		List<UUID> incomingIDs = Lists.newArrayList();
		for(Entry<EntityData, List<SpellData>> entry : this.activeSpells.entrySet())
			entry.getValue().forEach((spell) -> incomingIDs.add(spell.getUUID()));
//		System.out.println("Incoming spells: "+incomingIDs.size());
		
		// Identify all spells in the CANVAS_MAP that don't appear in the incoming UUIDs
		List<UUID> spellsToRemove = Lists.newArrayList();
		for(UUID spellID : CANVAS_MAP.keySet())
		{
			boolean identified = false;
			for(UUID incoming : incomingIDs)
				if(incoming.equals(spellID))
				{
					identified = true;
					break;
				}
			
			if(!identified)
				spellsToRemove.add(spellID);
		}
//		System.out.println("Removed spells: "+spellsToRemove.size());
		
		// Destroy all missing spells
		spellsToRemove.forEach((id) -> 
		{
			System.out.println("Expunged canvas for "+id.toString());
			CANVAS_MAP.get(id).close();
			CANVAS_MAP.remove(id);
		});
	}
	
	public List<Canvas> getSpellCanvasOn(Entity entity)
	{
		List<Canvas> canvases = Lists.newArrayList();
		getSpellsOn(entity).forEach((spell) -> 
		{
			UUID uuid = spell.getUUID();
			if(!CANVAS_MAP.containsKey(uuid))
				CANVAS_MAP.put(uuid, generateCanvasFor(spell.arrangement()));
			
			canvases.add(CANVAS_MAP.get(uuid));
		});
		
		return canvases;
	}
	
	private Canvas generateCanvasFor(ISpellComponent spell)
	{
		Canvas canvas = new Canvas(SpellTextureManager.getNewTexture(), 8);
		canvas.populate(spell);
		return canvas;
	}
	
	public void setDirty() { }
}
