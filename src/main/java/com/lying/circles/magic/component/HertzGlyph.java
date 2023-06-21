package com.lying.circles.magic.component;

import com.lying.circles.magic.variable.VariableSet;

public class HertzGlyph extends ComponentBase
{
	private final int tickRate;
	
	public HertzGlyph(int rate) { this.tickRate = rate; }
	
	public Category category() { return Category.HERTZ; }
	
	public Type type() { return Type.HERTZ; }
	
	public ComponentError getErrorState() { return ComponentError.GOOD; }
	
	public VariableSet execute(VariableSet variablesIn) { return variablesIn; }
	
	public int getTickRate() { return tickRate; }
}
