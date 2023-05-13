package com.lying.misc19.utility;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Cacheable entity value */
public class EntityData extends Tuple<UUID, BlockPos>
{
	private Entity cached = null;
	
	public EntityData(Entity entity)
	{
		this(entity.getUUID(), entity.blockPosition());
		cached = entity;
	}
	public EntityData(UUID uuidIn, BlockPos posIn) { super(uuidIn, posIn); }
	
	@Nullable
	public Entity findEntity(Level world)
	{
		if(cached != null && cached.isAlive() && world.isLoaded(cached.blockPosition()))
		{
			setB(cached.blockPosition());
			return cached;
		}
		else if(world.isLoaded(getB()))
		{
			AABB searchBounds = new AABB(-32, -32, -32, 32, 32, 32);
			for(Entity ent : world.getEntitiesOfClass(Entity.class, searchBounds.move(getB()), (entity) -> entity.isAlive() && entity.getUUID().equals(getA())))
			{
				this.cached = ent;
				return ent;
			}
		}
		
		return null;
	}
	
	public boolean equals(EntityData data) { return data.getA().equals(getA()) || data.cached == cached; }
	
	public CompoundTag save(CompoundTag nbt)
	{
		nbt.putUUID("UUID", getA());
		nbt.put("Pos", NbtUtils.writeBlockPos(getB()));
		return nbt;
	}
	
	public static EntityData load(CompoundTag nbt)
	{
		UUID uuid = nbt.getUUID("UUID");
		BlockPos pos = NbtUtils.readBlockPos(nbt.getCompound("Pos"));
		return new EntityData(uuid, pos);
	}
}