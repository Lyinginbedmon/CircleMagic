package com.lying.circles.utility;

import java.util.UUID;

import javax.annotation.Nullable;

import com.lying.circles.api.event.SpellEvent;
import com.lying.circles.init.SpellComponents;
import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.magic.component.RootGlyph;
import com.lying.circles.magic.variable.IVariable;
import com.lying.circles.magic.variable.VarBool;
import com.lying.circles.magic.variable.VarDouble;
import com.lying.circles.magic.variable.VarEntity;
import com.lying.circles.magic.variable.VarLevel;
import com.lying.circles.magic.variable.VariableSet;
import com.lying.circles.magic.variable.VariableSet.Slot;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

/** Stores and executes spells */
public class SpellData
{
	private final ISpellComponent arrangement;
	private VariableSet variables = new VariableSet();
	
	private EntityData ownerData;
	private int ticks = 0;
	
	private UUID spellUUID = null;
	private boolean isAlive = true;
	
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
	public boolean isAlive() { return this.isAlive; }
	
	public void setUUID(UUID uuidIn) { this.spellUUID = uuidIn; }
	public UUID getUUID() { return this.spellUUID; }
	
	public void kill(){ this.isAlive = false; }
	
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
		{
			if(!this.variables.isUsing(Slot.CASTER))
				this.variables.set(Slot.CASTER, new VarEntity(caster));
		}
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
				Vec3 position = variables.get(Slot.POSITION).asVec();
				MinecraftForge.EVENT_BUS.post(new SpellEvent.Run(root, world, position, caster.getUUID()));
				this.variables.set(Slot.CONTINUE, VarBool.FALSE);
				root.performExecution(world, caster, this.variables);
				this.variables.set(Slot.AGE, new VarDouble(this.variables.get(Slot.AGE).asDouble() + 1));
				if(!this.variables.get(Slot.CONTINUE).asBoolean())
				{
					MinecraftForge.EVENT_BUS.post(SpellEvent.End.die(root, world, position, caster.getUUID()));
					kill();
				}
			}
		}
		else
		{
			// Spells cannot be executed without a root component, and the existence of one attempting to do so suggests a significant error
			Vec3 position = variables.get(Slot.POSITION).asVec();
			UUID casterID = ((VarEntity)variables.get(Slot.CASTER)).uniqueID();
			MinecraftForge.EVENT_BUS.post(SpellEvent.End.error(root, world, position, casterID));
			kill();
		}
		
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
		nbt.putBoolean("Alive", isAlive);
		nbt.put("Vars", variables.writeToNBT(new CompoundTag()));
		nbt.putInt("Ticks", ticks);
		return nbt;
	}
	
	public static SpellData loadFrom(CompoundTag nbt)
	{
		SpellData data = new SpellData(SpellComponents.readFromNBT(nbt.getCompound("Spell")), EntityData.load(nbt.getCompound("Owner")));
		data.variables = VariableSet.readFromNBT(nbt.getCompound("Vars"));
		data.setUUID(nbt.getUUID("UUID"));
		data.isAlive = nbt.getBoolean("Alive");
		data.ticks = nbt.getInt("Ticks");
		return data;
	}
	
	public ISpellComponent arrangement() { return this.arrangement; }
}