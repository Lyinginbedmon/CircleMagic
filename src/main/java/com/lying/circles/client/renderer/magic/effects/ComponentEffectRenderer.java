package com.lying.circles.client.renderer.magic.effects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

@FunctionalInterface
public interface ComponentEffectRenderer
{
	public void render(Level world, Vec3 origin, CompoundTag compound, int ticksActive, final RenderLevelStageEvent renderEvent);
}
