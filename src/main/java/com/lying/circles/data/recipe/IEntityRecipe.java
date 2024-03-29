package com.lying.circles.data.recipe;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IEntityRecipe
{
	@Nullable
	public Entity createEntityAt(Level world, Vec3 pos);
}
