package com.lying.misc19.item;

import javax.annotation.Nullable;

import com.lying.misc19.magic.ISpellComponent;

import net.minecraft.nbt.CompoundTag;

public interface ISpellContainer
{
	@Nullable
	public ISpellComponent getSpell(CompoundTag tag);
}
