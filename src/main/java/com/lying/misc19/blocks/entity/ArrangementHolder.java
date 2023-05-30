package com.lying.misc19.blocks.entity;

import javax.annotation.Nullable;

import com.lying.misc19.magic.ISpellComponent;

public interface ArrangementHolder
{
	public default boolean hasArrangement() { return arrangement() != null; }
	
	@Nullable
	public ISpellComponent arrangement();
	
	public void setArrangement(ISpellComponent spell);
	
	public int glyphCap();
}
