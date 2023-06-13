package com.lying.circles.blocks.entity;

import java.util.function.Function;

import com.lying.circles.init.CMBlockEntities;
import com.lying.circles.init.CMEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class StatueBlockEntity extends BlockEntity
{
	private Entity displayEntity;
	
	public StatueBlockEntity(BlockPos p_155229_, BlockState p_155230_)
	{
		super(CMBlockEntities.STATUE.get(), p_155229_, p_155230_);
	}
	
	public Entity getStatue(Level world)
	{
		if(displayEntity == null)
		{
			CompoundTag tag = new CompoundTag();
			tag.putString("id", CMEntities.STATUE.getId().toString());
			displayEntity = EntityType.loadEntityRecursive(tag, world, Function.identity());
		}
		return displayEntity;
	}
}
