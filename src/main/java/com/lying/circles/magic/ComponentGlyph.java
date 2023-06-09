package com.lying.circles.magic;

import com.lying.circles.magic.component.ComponentBase;
import com.lying.circles.magic.variable.VariableSet;

public abstract class ComponentGlyph extends ComponentBase
{
	public Type type() { return Type.OPERATION; }
	
	public static class Dummy extends ComponentGlyph
	{
		public Category category() { return Category.OPERATION; }
		
		public VariableSet execute(VariableSet variablesIn) { return variablesIn; }
		
		public int castingCost() { return 0; }
		
		public boolean playerPlaceable() { return false; }
	}
}
