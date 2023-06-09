package com.lying.circles.magic;

import javax.annotation.Nullable;

import com.lying.circles.init.SpellComponents;

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
	
	@Nullable
	public static Element fromString(String nameIn)
	{
		for(Element element : values())
			if(element.getSerializedName().equalsIgnoreCase(nameIn))
				return element;
		return null;
	}
}
