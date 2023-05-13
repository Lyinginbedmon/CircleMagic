package com.lying.misc19.magic.variable;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.misc19.magic.variable.VariableSet.VariableType;
import com.lying.misc19.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class VarEntity extends VariableBase
{
	/** The entity as it actually exists in the world */
	private Entity value = null;
	/** An imaginary version of the entity based on cached data */
	private Entity cached = null;
	
	/** The entity's UUID */
	private UUID uniqueID;
	/** The entity's last known position */
	private Vec3 position;
	/** The save NBT data of the entity, used to create the cached clone */
	private CompoundTag saveData;
	
	public VarEntity(Entity entityIn)
	{
		this.value = entityIn;
		
		if(entityIn != null)
			setCacheValues(entityIn);
	}
	
	public VariableType type() { return VariableType.ENTITY; }
	
	public Component translate()
	{
		return Component.translatable("variable."+Reference.ModInfo.MOD_ID+".entity", asEntity().getDisplayName().getString());
	}
	
	public Entity asEntity()
	{
		if(value != null)
			return this.value;
		
		if(cached != null)
			return cached;
		else
		{
			cached = makeFakeEntity(saveData);
			if(cached != null)
				return cached;
		}
		
		return makeFakeEntity();
	}
	
	public Vec3 asVec() { return this.value.position(); }
	
	public boolean asBoolean() { return asEntity().isAlive(); }
	
	public boolean greater(IVariable var2) { return false; }
	
	public IVariable add(@Nonnull IVariable var2) { return copy(); }
	
	public IVariable subtract(@Nonnull IVariable var2) { return copy(); }
	
	public IVariable multiply(@Nonnull IVariable var2) { return copy(); }
	
	public IVariable divide(@Nonnull IVariable var2) { return copy(); }
	
	private VarEntity copy()
	{
		VarEntity duplicate = new VarEntity(this.value);
		duplicate.cached = this.cached;
		duplicate.load(save(new CompoundTag()));
		
		return duplicate;
	}
	
	public CompoundTag save(CompoundTag compound)
	{
		if(uniqueID != null)
			compound.put("UUID", NbtUtils.createUUID(uniqueID));
		
		if(position != null)
		{
			CompoundTag pos = new CompoundTag();
			pos.putDouble("X", position.x);
			pos.putDouble("Y", position.y);
			pos.putDouble("Z", position.z);
			compound.put("Position", pos);
		}
		
		if(saveData != null && !saveData.isEmpty())
			compound.put("NBT", saveData);
		
		return compound;
	}
	
	public void load(CompoundTag compound)
	{
		if(compound.contains("UUID", Tag.TAG_INT_ARRAY))
			this.uniqueID = NbtUtils.loadUUID(compound.get("UUID"));
		
		if(compound.contains("Position", Tag.TAG_COMPOUND))
		{
			CompoundTag pos = compound.getCompound("Position");
			this.position = new Vec3(pos.getDouble("X"), pos.getDouble("Y"), pos.getDouble("Z"));
		}
		
		if(compound.contains("NBT", Tag.TAG_COMPOUND))
			this.saveData = compound.getCompound("NBT");
	}
	
	private void setCacheValues(Entity entityIn)
	{
		uniqueID = entityIn.getUUID();
		position = entityIn.position();
		
		saveData = new CompoundTag();
		entityIn.save(saveData);
	}
	
	public void recache(Level worldIn)
	{
		if(value != null)
		{
			// Keep cache values up to date in case we lose track again
			setCacheValues(this.value);
			return;
		}
		
		if(this.position != null && this.uniqueID != null)
			for(Entity ent : worldIn.getEntitiesOfClass(Entity.class, AABB.ofSize(this.position, 16D, 16D, 16D)))
			{
				if(ent.getUUID().equals(this.uniqueID))
				{
					this.value = ent;
					setCacheValues(ent);
					break;
				}
			}
	}
	
	@Nonnull
	public static Entity makeFakeEntity()
	{
		// FIXME Create non-null default fake entity
		
		return null;
	}
	
	@Nullable
	private static Entity makeFakeEntity(CompoundTag data)
	{
		Optional<Entity> ent = null;
		try
		{
			ent = EntityType.create(data, null);
		}
		catch(Exception e) { }
		return ent.isPresent() ? null : ent.get();
	}
}