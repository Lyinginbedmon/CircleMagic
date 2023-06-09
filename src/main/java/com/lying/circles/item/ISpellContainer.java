package com.lying.circles.item;

import javax.annotation.Nullable;

import com.lying.circles.magic.ISpellComponent;

import net.minecraft.nbt.CompoundTag;

public interface ISpellContainer
{
	@Nullable
	public ISpellComponent getSpell(CompoundTag tag);
}
