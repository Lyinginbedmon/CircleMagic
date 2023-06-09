package com.lying.circles.blocks.entity;

import javax.annotation.Nullable;

import com.lying.circles.magic.ISpellComponent;

public interface ArrangementHolder
{
	public default boolean hasArrangement() { return arrangement() != null; }
	
	@Nullable
	public ISpellComponent arrangement();
	
	public void setArrangement(ISpellComponent spell);
	
	public int glyphCap();
}
