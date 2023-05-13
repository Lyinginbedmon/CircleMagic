package com.lying.misc19.utility;

import java.util.UUID;

import javax.annotation.Nullable;

import com.lying.misc19.init.SpellComponents;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.component.RootGlyph;
import com.lying.misc19.magic.variable.IVariable;
import com.lying.misc19.magic.variable.VarBool;
import com.lying.misc19.magic.variable.VarDouble;
import com.lying.misc19.magic.variable.VarEntity;
import com.lying.misc19.magic.variable.VarLevel;
import com.lying.misc19.magic.variable.VariableSet;
import com.lying.misc19.magic.variable.VariableSet.Slot;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** Stores and executes spells */
public class SpellData
{
	private final ISpellComponent arrangement;
	private VariableSet variables = new VariableSet();
	
	private EntityData ownerData;
	private int ticks = 0;
	
	private UUID spellUUID = null;
	
	public SpellData(ISpellComponent spellIn, LivingEntity casterIn)
	{
		this(spellIn, casterIn.getUUID(), casterIn.blockPosition());
		this.variables = ((RootGlyph)spellIn).populateCoreVariables(casterIn.getLevel(), casterIn, variables);
		this.ownerData.findEntity(casterIn.getLevel());
	}
	
	public SpellData(ISpellComponent spellIn, UUID ownerID, BlockPos ownerPos)
	{
		this(spellIn, new EntityData(ownerID, ownerPos));
	}
	
	public SpellData(ISpellComponent spellIn, EntityData ownerIn)
	{
		this.arrangement = spellIn;
		this.ownerData = ownerIn;
	}
	
	/** Returns true if this spell has been registered with the world */
	public boolean isAlive() { return this.spellUUID != null; }
	
	public void setUUID(UUID uuidIn) { this.spellUUID = uuidIn; }
	public UUID getUUID() { return this.spellUUID; }
	
	public void kill() { this.spellUUID = null; }
	
	public IVariable getVariable(Slot name) { return this.variables.get(name); }
	
	/** Executes the spell, returns false if the spell should be dropped */
	public boolean executeSpell(ServerLevel world)
	{
		if(!isAlive())
			return false;
		
		variables.set(Slot.UUID1, new VarDouble(getUUID().getLeastSignificantBits()));
		variables.set(Slot.UUID2, new VarDouble(getUUID().getMostSignificantBits()));
		variables.set(Slot.WORLD, new VarLevel(world));
		LivingEntity caster = getOwner(world);
		
		// If we can't find the caster, we won't know who to bill for the mana
		if(caster != null)
			this.variables.set(Slot.CASTER, new VarEntity(caster));
		else
			return true;
		
		RootGlyph root = null;
		try
		{
			root = (RootGlyph)arrangement;
		}
		catch(Exception e) { e.printStackTrace(); }
		
		if(root != null)
		{
			if(ticks++ % root.tickRate() == 0)
			{
				this.variables.set(Slot.CONTINUE, VarBool.FALSE);
				root.performExecution(world, caster, this.variables);
				this.variables.set(Slot.AGE, new VarDouble(this.variables.get(Slot.AGE).asDouble() + 1));
				if(!this.variables.get(Slot.CONTINUE).asBoolean())
					kill();
			}
		}
		else
			kill();
		
		return isAlive();
	}
	
	@Nullable
	public LivingEntity getOwner(Level world)
	{
		return (LivingEntity)this.ownerData.findEntity(world);
	}
	
	public CompoundTag saveToNbt(CompoundTag nbt)
	{
		nbt.putUUID("UUID", this.spellUUID);
		nbt.put("Spell", ISpellComponent.saveToNBT(arrangement));
		nbt.put("Owner", this.ownerData.save(new CompoundTag()));
		
		nbt.put("Vars", variables.writeToNBT(new CompoundTag()));
		nbt.putInt("Ticks", ticks);
		return nbt;
	}
	
	public static SpellData loadFrom(CompoundTag nbt)
	{
		SpellData data = new SpellData(SpellComponents.readFromNBT(nbt.getCompound("Spell")), EntityData.load(nbt.getCompound("Owner")));
		data.variables = VariableSet.readFromNBT(nbt.getCompound("Vars"));
		data.setUUID(nbt.getUUID("UUID"));
		data.ticks = nbt.getInt("Ticks");
		return data;
	}
	
	public ISpellComponent arrangement() { return this.arrangement; }
}