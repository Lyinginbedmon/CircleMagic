package com.lying.misc19.magic.component;

import com.lying.misc19.magic.variable.VariableSet;

public class HertzGlyph extends ComponentBase
{
	private final int tickRate;
	
	public HertzGlyph(int rate) { this.tickRate = rate; }
	
	public Category category() { return Category.HERTZ; }
	
	public Type type() { return Type.HERTZ; }
	
	public VariableSet execute(VariableSet variablesIn) { return variablesIn; }
	
	public int getTickRate() { return tickRate; }
}
