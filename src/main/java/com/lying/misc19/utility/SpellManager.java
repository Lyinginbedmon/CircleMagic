package com.lying.misc19.utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.lying.misc19.client.ClientSetupEvents;
import com.lying.misc19.entities.SpellEntity;
import com.lying.misc19.network.PacketHandler;
import com.lying.misc19.network.PacketSyncSpellManager;
import com.lying.misc19.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

public class SpellManager extends SavedData
{
	protected static SpellManager INSTANCE = null;
	protected static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_spell_manager";
	
	private Map<EntityData, List<SpellData>> activeSpells = new HashMap<>();
	
	private Level world;
	private int ticks = 0;
	
	private SpellManager() { this(null); }
	public SpellManager(@Nullable Level worldIn)
	{
		this.world = worldIn;
	}
	
	public static SpellManager instance(Level worldIn)
	{
		if(worldIn.isClientSide())
			return ClientSetupEvents.getLocalData();
		
		if(INSTANCE == null)
		{
			ServerLevel overworld = ((ServerLevel)worldIn).getServer().getLevel(Level.OVERWORLD);
			INSTANCE = overworld.getDataStorage().computeIfAbsent(SpellManager::fromNbt, SpellManager::new, DATA_NAME);
			INSTANCE.world = worldIn;
		}
		return INSTANCE;
	}
	
	public static SpellManager fromNbt(CompoundTag tag)
	{
		SpellManager manager = new SpellManager();
		manager.read(tag);
		return manager;
	}
	
	public CompoundTag save(CompoundTag data)
	{
		ListTag entries = new ListTag();
		for(Entry<EntityData, List<SpellData>> entry : this.activeSpells.entrySet())
		{
			if(entry.getValue().isEmpty())
				continue;
			
			CompoundTag nbt = new CompoundTag();
			nbt.put("Entity", entry.getKey().save(new CompoundTag()));
			
			ListTag spells = new ListTag();
			for(SpellData spell : entry.getValue())
				if(spell.isAlive())
					spells.add(spell.saveToNbt(new CompoundTag()));
			nbt.put("Spells", spells);
			
			entries.add(nbt);
		}
		data.put("Entries", entries);
		return data;
	}
	
	public void read(CompoundTag data)
	{
		this.activeSpells.clear();
		ListTag entries = data.getList("Entries", Tag.TAG_COMPOUND);
		for(int i=0; i<entries.size(); i++)
		{
			CompoundTag nbt = entries.getCompound(i);
			EntityData entity = EntityData.load(nbt.getCompound("Entity"));
			List<SpellData> spells = Lists.newArrayList();
			ListTag spellTags = nbt.getList("Spells", Tag.TAG_COMPOUND);
			for(int j=0; j<spellTags.size(); j++)
			{
				SpellData spell = SpellData.loadFrom(spellTags.getCompound(j));
				if(spell.isAlive())
					spells.add(spell);
			}
			this.activeSpells.put(entity, spells);
		}
	}
	
	public void tick()
	{
		if(isEmpty() || this.world == null || this.world.isClientSide())
			return;
		
		ServerLevel serverWorld = (ServerLevel)this.world;
		
		List<EntityData> clearEntities = Lists.newArrayList();
		for(EntityData entity : this.activeSpells.keySet())
		{
			Entity holder = entity.findEntity(serverWorld);
			
			// Skip any parent entities we can't find
			if(holder == null)
				continue;
			
			List<SpellData> activeSpells = this.activeSpells.get(entity);
			activeSpells.removeAll(getDeadSpells(serverWorld, activeSpells));
			if(activeSpells.isEmpty())
				clearEntities.add(entity);
		}
		clearEntities.forEach((entity) -> this.activeSpells.remove(entity));
		
		if(ticks++ % 10 == 0)
			setDirty();
	}
	
	public boolean isEmpty() { return this.activeSpells.isEmpty(); }
	
	private static List<SpellData> getDeadSpells(ServerLevel serverWorld, List<SpellData> spellEntities)
	{
		List<SpellData> deadSpells = Lists.newArrayList();
		spellEntities.forEach((spell) -> { if(!spell.executeSpell(serverWorld)) deadSpells.add(spell); });
		return deadSpells;
	}
	
	/** Adds the given spell to the world attached to the given entity, returns its UUID */
	public UUID addSpellOn(SpellData spell, Entity onEntity)
	{
		spell.setUUID(UUID.randomUUID());
		EntityData entity = new EntityData(onEntity);
		List<SpellData> active = getSpellsOn(onEntity);
		active.add(spell);
		this.activeSpells.put(entity, active);
		
		setDirty();
		return spell.getUUID();
	}
	
	public List<SpellData> getSpellsOn(Entity onEntity)
	{
		EntityData entity = new EntityData(onEntity);
			for(Entry<EntityData, List<SpellData>> entry : this.activeSpells.entrySet())
				if(entry.getKey().equals(entity))
					return entry.getValue();
		return Lists.newArrayList();
	}
	
	public void removeSpellsFrom(Entity onEntity)
	{
		this.activeSpells.remove(new EntityData(onEntity));
		setDirty();
	}
	
	public static List<SpellData> getSpellsWithin(Level world, AABB bounds)
	{
		List<SpellData> spells = Lists.newArrayList();
		world.getEntitiesOfClass(SpellEntity.class, bounds, (ent) -> ent.isAlive() && !ent.isVanishing()).forEach((ent) -> spells.add(ent.getSpell()));
		
		SpellManager manager = SpellManager.instance(world);
		world.getEntitiesOfClass(LivingEntity.class, bounds, (ent) -> ent.isAlive()).forEach((ent) -> spells.addAll(manager.getSpellsOn(ent)));
		return spells;
	}
	
	public void setDirty()
	{
		super.setDirty();
		
		if(!this.world.isClientSide())
			PacketHandler.sendToAll((ServerLevel)this.world, new PacketSyncSpellManager(this));
	}
}
