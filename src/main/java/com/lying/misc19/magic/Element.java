package com.lying.misc19.magic;

import com.lying.misc19.init.SpellComponents;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum Element implements StringRepresentable
{
	ORIGO,
	MUNDUS,
	ARDERE,
	MARE,
	FINIS,
	SCULK;
	
	public String getSerializedName() { return name().toLowerCase(); }
	
	public ResourceLocation glyph() { return SpellComponents.make(getSerializedName()+"_element"); }
}
